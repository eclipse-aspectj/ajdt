/*******************************************************************************
 * Copyright (c) 2007 Linton Ye.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Linton Ye - initial API and implementation
 ******************************************************************************/
package org.eclipse.ajdt.pointcutdoctor.core.relaxer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.ArgsPointcut;
import org.aspectj.weaver.patterns.HandlerPointcut;
import org.aspectj.weaver.patterns.KindedPointcut;
import org.aspectj.weaver.patterns.NotPointcut;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.PointcutRewriter;
import org.aspectj.weaver.patterns.ThisOrTargetPointcut;
import org.aspectj.weaver.patterns.WithinPointcut;
import org.aspectj.weaver.patterns.WithincodePointcut;
import org.eclipse.ajdt.pointcutdoctor.core.fpointcut.FlatternedPointcut;
import org.eclipse.ajdt.pointcutdoctor.core.utils.PointcutUtil;



public class PointcutRelaxController {
	
	private int maxRelaxersToApply;
//	private Map<Relaxer, Pointcut> relaxerPtcMap = new HashMap<Relaxer, Pointcut>();
	private World world;

	public PointcutRelaxController(int maxRelaxers) {
		this.maxRelaxersToApply = maxRelaxers;
	}
	
	public List<RelaxedPointcut> createRelaxedPointcuts(Pointcut pointcut, World w) {
		this.world = w;
		
		// since all pointcuts can be rewritten in the form: (..&..&..) || (..&..&..) ||...
		//  and then flattern these && and || for easier handling
		Pointcut nptc = (new PointcutRewriter()).rewrite(pointcut);
		//  XXX the rewriter will convert some pointcuts that are obvious empty to Pointcut.MatchNothingPointcut
		//  we need to write our own rewriter instead...
		if (nptc.toString().length()==0) nptc = pointcut; 
		Pointcut fptc = PointcutUtil.flattern(nptc);
//		relaxerPtcMap.clear();
		
		
		List<Pointcut> results;

		if (fptc instanceof FlatternedPointcut) {
			FlatternedPointcut ffptc = (FlatternedPointcut)fptc;
			if (ffptc.getKind()==FlatternedPointcut.Kind.OR) {			
				results = new ArrayList<Pointcut>();
				for (Pointcut ptc: ffptc.getChildren()) {
					List<Pointcut> relaxedPtcs = relaxPointcut(ptc);
					results.addAll(relaxedPtcs);
				}
			} else results = relaxPointcut(fptc);
		} else results = relaxPointcut(fptc);

		List<RelaxedPointcut> rresults = new LinkedList<RelaxedPointcut>();
		// post processing: convert FlatternedPointcut to regular AndPointcut or OrPointcut 
		for (int i=0;i<results.size();i++) {
			Pointcut ptc = results.get(i);
			createRelaxedPointcutAndAddToList(ptc, rresults);
		}
		return rresults;
	}

	
	private void createRelaxedPointcutAndAddToList(Pointcut p, List<RelaxedPointcut> rresults) {
		Pointcut pactual = p;
		if (p instanceof FlatternedPointcut)
			pactual = ((FlatternedPointcut)p).toHierachichalPointcut();
		rresults.add(new RelaxedPointcut(pactual));
	}

	//TODO assume we only have && in this pointcut for now
	private List<Pointcut> relaxPointcut(Pointcut pointcut) {
		
		List<Pointcut> results = new ArrayList<Pointcut>();
		
		List<List<RelaxData>> allRelaxData = createRelaxData(pointcut);
		
		Map<Pointcut, Pointcut> pivotsMap = new HashMap<Pointcut, Pointcut>();
		for (List<RelaxData> relaxData:allRelaxData) {
			if (relaxData!=null && !relaxData.isEmpty()) { //XXX why for jEdit relaxData==null?
				//remove pointcuts that are marked to be removed according to the relaxing rules
				List<Pointcut> toRemove = new LinkedList<Pointcut>();
				pivotsMap.clear();
				for (RelaxData d:relaxData)
					if (d.getRelaxOp()==RelaxOp.Remove) toRemove.add(d.getPointcut());
					else pivotsMap.put(d.getPointcut(), null);
				Pointcut newPtc = PointcutUtil.cloneAndRemoveWithPivots(pointcut, toRemove, pivotsMap);
				// update
				updateParts(relaxData, pivotsMap, newPtc);
				results.add(newPtc);
			}
		}
		
		return results;
	}
	
	private void updateParts(List<RelaxData> relaxData, Map<Pointcut, Pointcut> pivotsMap, Pointcut newPtc) {
		for (RelaxData data:relaxData) {
			if (data.getRelaxOp()==RelaxOp.Replace) {
				Object mainObj = null;
				Pointcut ptc = pivotsMap.get(data.getPointcut()); // get the corresponding pointcut in the cloned instance
				if (ptc instanceof KindedPointcut) 
					mainObj = ((KindedPointcut)ptc).getSignature();
				else if (ptc instanceof HandlerPointcut)
					mainObj = ptc;
				
				updateField(mainObj, data.getFieldName(), data.getReplacement());
			}
		}
	}
	
	private void updateField(Object mainObj, String fieldName, Object replacement) {
		if (mainObj!=null) {
			Class<? extends Object> clz = mainObj.getClass();
			try {
				Field field = clz.getDeclaredField(fieldName);
				field.setAccessible(true);
				field.set(mainObj, replacement);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
		}
	}

	private List<List<RelaxData>> createRelaxData(Pointcut pointcut) {
//		RelaxDataCollections results = new RelaxDataCollections();

		List<Relaxer> effectiveRelaxers = prepareRelaxers(pointcut);
		
		List<List<RelaxData>> results = new ArrayList<List<RelaxData>>(); 
		
		createRelaxDataRecursively(pointcut, 0, effectiveRelaxers, null, results);
		
		return results;
	}

	
//	private void doRelax(List<Relaxer> relaxers, Pointcut ptc, List<RelaxedPointcut> results) {
//		int n = maxRelaxersToApply;
//		if (n<=0) n = relaxers.size();
//		
//		List<RelaxOp[]> allRelaxOps = new ArrayList<RelaxOp[]>(); 
//		relaxRecursively(0, n, relaxers, ptc, allRelaxOps);
//		for (RelaxOp[] relaxOps:allRelaxOps) {
//			RelaxedPointcut rptc = relax(ptc, relaxOps);
//			results.add(rptc);
//		}
//	}
//
//	private void relaxRecursively(int idx, int max, List<Relaxer> relaxers, Pointcut ptc, List<RelaxOp[]> allops) {
//		if (idx<relaxers.size() && idx<max) {
//			Relaxer relaxer = relaxers.get(idx);
//			RelaxOp[] ops = relaxer.relax(ptc, world);
//			for (RelaxOp op:ops) {
//				allops.add(op);
//				relaxRecursively(idx+1, max, relaxers, ptc, results);
//			}
//		}
//	}


	
	private void createRelaxDataRecursively(Pointcut ptc, int idx, List<Relaxer> relaxers, 
			List<RelaxData> row, List<List<RelaxData>> allData) {
		if (idx<relaxers.size()) {
			Relaxer relaxer = relaxers.get(idx);
			List<RelaxData> data = relaxer.relax(world);
			List<RelaxData> newRow;
			if (row!=null) newRow = new ArrayList<RelaxData>(row);  //TODO maybe a lot of memory consumed?
			else newRow = new ArrayList<RelaxData>();
			if (data!=null && !data.isEmpty())
				for (RelaxData d:data) {
					newRow.add(d);
					createRelaxDataRecursively(ptc, idx+1, relaxers, newRow, allData);
				}
			else createRelaxDataRecursively(ptc, idx+1, relaxers, newRow, allData);
		} else {
			allData.add(row);
		}
	}

//	private void createRelaxDataRecursively(Pointcut ptc, int idx, List<Relaxer> relaxers, 
//			RelaxDataCollections results) {
//		if (idx<relaxers.size()) {
//			Relaxer relaxer = relaxers.get(idx);
//			List<RelaxData> data = relaxer.relax(ptc, world);
//			if (data!=null && !data.isEmpty())
//				for (RelaxData d:data) {
//					results.addToCurrentRow(d);
//					createRelaxDataRecursively(ptc, idx+1, relaxers, results);
//				}
//			else createRelaxDataRecursively(ptc, idx+1, relaxers, results);
//		} else {
//			results.nextRow();
//		}
//	}
//
	private List<Relaxer> prepareRelaxers(Pointcut pointcut) {
		List<Relaxer> relaxers = new ArrayList<Relaxer>();
		if (pointcut instanceof FlatternedPointcut) {
			FlatternedPointcut fptc = (FlatternedPointcut)pointcut;
			if (fptc.getKind()==FlatternedPointcut.Kind.AND) {
				for (Pointcut ptc:fptc.getChildren()) {
					createRelaxersForAtomicPointcut(relaxers, ptc);
				}
			} else
				throw new Error("pointcut not well-formed!:"+pointcut.toString()); //TODO
		} else
			createRelaxersForAtomicPointcut(relaxers, pointcut);
//		if (pointcut instanceof KindedPointcut) {
//			relaxers = createKindedPointcutRelaxers((KindedPointcut) pointcut);
//		}

		sortRelaxersUsingHeuristics(relaxers, pointcut);
		
		if (maxRelaxersToApply>0 && maxRelaxersToApply<relaxers.size())
			relaxers = relaxers.subList(0, maxRelaxersToApply-1);
		
		return relaxers;
	}

	private void createRelaxersForAtomicPointcut(List<Relaxer> relaxers, Pointcut ptc) {
		if (ptc instanceof ArgsPointcut)
			addRelaxer(relaxers, new ArgsRelaxer(),ptc);
		else if (ptc instanceof ThisOrTargetPointcut) {
			addRelaxer(relaxers, new ThisOrTargetRelaxer(),ptc);
		}
		else if (ptc instanceof WithinPointcut) 
			addRelaxer(relaxers, new WithinRelaxer(),ptc);
		else if (ptc instanceof WithincodePointcut)
			addRelaxer(relaxers, new WithincodeRelaxer(),ptc);
		else if (ptc instanceof NotPointcut) 
			addRelaxer(relaxers, new NotRelaxer(), ptc);
		else if (ptc instanceof HandlerPointcut)
			addRelaxer(relaxers, new HandlerRelaxer(),ptc);
		else if (ptc instanceof KindedPointcut)
			relaxers.addAll(createKindedPointcutRelaxers((KindedPointcut) ptc));
		//TODO what else?
	}

	private void sortRelaxersUsingHeuristics(List<Relaxer> relaxers, Pointcut pointcut) {
		/*
		 * ParamsRelaxer > ArgsRelaxer > AnnotationRelaxer > 
		 * ModifierRelaxer > ThrowRelaxer > ReturnTypeRelaxer >
		 * HandlerRelaxer > DeclaringTypeRelaxer > ThisOrTargetRelaxer > 
		 * WithinRelaxer > NameRelaxer
		 */
		final Map<Class<?>, Integer> keyMap = new HashMap<Class<?>, Integer>();
		int k = 0;
		keyMap.put(NotRelaxer.class, k++);
		keyMap.put(ParamsRelaxer.class, k++);
		keyMap.put(ArgsRelaxer.class, k++);
		keyMap.put(AnnotationRelaxer.class, k++);
		keyMap.put(ModifierRelaxer.class, k++);
		keyMap.put(ThrowRelaxer.class, k++);
		keyMap.put(ReturnTypeRelaxer.class, k++);
		keyMap.put(HandlerRelaxer.class, k++);
		keyMap.put(DeclaringTypeRelaxer.class, k++);
		keyMap.put(ThisOrTargetRelaxer.class, k++);
		keyMap.put(WithinRelaxer.class, k++);
		keyMap.put(NameRelaxer.class, k++);
		
		Collections.sort(relaxers, new Comparator<Relaxer>() {
			public int compare(Relaxer o1, Relaxer o2) {
				Integer k1 = keyMap.get(o1.getClass());
				Integer k2 = keyMap.get(o2.getClass());
				if (k1!=null && k2!=null)
					return k1.compareTo(k2);
				else if (k1==null && k2==null) return 0;
				else if (k1==null) return -1;
				else return 1;
			}
		});
	}

	private List<Relaxer> createKindedPointcutRelaxers(KindedPointcut ptc) {
		List<Relaxer> relaxers = new ArrayList<Relaxer>();
		addRelaxer(relaxers, new ReturnTypeRelaxer(),ptc);
		addRelaxer(relaxers, new ParamsRelaxer(),ptc);
		addRelaxer(relaxers, new DeclaringTypeRelaxer(),ptc);
		addRelaxer(relaxers, new ThrowRelaxer(),ptc);
		addRelaxer(relaxers, new AnnotationRelaxer(),ptc);
		addRelaxer(relaxers, new ModifierRelaxer(),ptc);
		addRelaxer(relaxers, new NameRelaxer(),ptc);
		
		return relaxers;
	}
	
	private void addRelaxer(List<Relaxer> relaxers, Relaxer relaxer, Pointcut correspondingPtc) {
		relaxer.setAffectingPointcut(correspondingPtc);
		relaxers.add(relaxer);
//		relaxerPtcMap.put(relaxer, correspondingPtc);
	}

//	protected void doRelax(List<Relaxer> relaxers, int currentDepth, RelaxedPointcut pointcut, List<RelaxedPointcut> relaxedPtcs) {
//		// only apply the first "relaxDepth" relaxers
//		if (currentDepth<relaxers.size() && currentDepth<maxRelaxersToApply) {
//			List<RelaxedPointcut> relaxedByOne = relaxers.get(currentDepth).relax(pointcut, world);
//			for (RelaxedPointcut ptc:relaxedByOne)
//				relaxedPtcs.add(ptc);
//			doRelax(relaxers, currentDepth+1, pointcut, relaxedPtcs);
//			for (RelaxedPointcut ptc:relaxedByOne) {
//				doRelax(relaxers, currentDepth+1, ptc, relaxedPtcs);
//			}
//		}
//	}
}
