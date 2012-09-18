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
package org.eclipse.ajdt.pointcutdoctor.core.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StringUtils {
	public static String getPathFromFilename(String filename) {
		int idx = filename.lastIndexOf(File.separator);
		if (idx>0) {
			return filename.substring(0, idx);
		} else return filename;
	}
	
	public static String arrayToString(Object[] args) {
		String s = "";
		for(Object a:args) {
			if (s.length()>0) s+=", ";
			s+=a;
		}
		return "("+s+")";
	}


	
	
	/**
	 * Construct the longest common substring between two strings if such
	 * a substring exists. Note that this is different from the longest
	 * common subsequence in that it assumes you want the longest 
	 * continuous sequence. The cost of this routine can be made less by
	 * keeping a master copy of data around that you want to check input
	 * against. That is, imagine that you keep the sorted suffix arrays
	 * around for some collection of data items. Then finding the LCS
	 * against that set is just a matter of computing the suffix matrix
	 * for the input (e.g., line) and comparing against the pre-computed
	 * suffix arrays for each data item.
	 * <p>
	 * In any event, this routine always computes and sorts the suffix 
	 * arrays for both input string parameters.
	 * 
	 * Courtesy of Michael E. Locasto at 
	 * http://fae.cs.columbia.edu/media/LongestCommonSubstring.java
	 *
	 * @param left the first string instance
	 * @param right the second string instance
	 * @return the longest common substring, or the empty string if
	 *         at least one of the arguments are <code>null</code>, empty,
	 *         or there is no match.
	 */
	public static String longestCommonSubstr(String left, String right) {
		/* BEFORE WE ALLOCATE ANY DATA STORAGE, VALIDATE ARGS */
		if(null==left || "".equals(left))
			return "";
		if(null==right || "".equals(right))
			return "";      
		if(left.equals(right))
			return left;
		
		/* ALLOCATE VARIABLES WE'LL NEED FOR THE ROUTINE */
		StringBuffer bestMatch = new StringBuffer(0);
		StringBuffer currentMatch = new StringBuffer(1024);
		ArrayList<String> dataSuffixList = new ArrayList<String>();
		ArrayList<String> lineSuffixList = new ArrayList<String>();
		String shorter = null;
		String longer = null; 
		
		if(left.length()<right.length())
		{
			shorter = left;
			longer = right;
		}else{
			shorter = right;
			longer = left;
		}
		
		/* Using some builtin String methods, take a couple of shortcuts */ 
		if(longer.startsWith(shorter))
		{
			return shorter;
		}else if(longer.endsWith(shorter)){
			return shorter;
		}
		
		/* FIRST, COMPUTE SUFFIX ARRAYS */
		for(int i=0;i<left.length();i++)
		{
			dataSuffixList.add(left.substring(i,left.length()));
		}
		for(int i=0;i<right.length();i++)
		{
			lineSuffixList.add(right.substring(i,right.length()));
		}
		
		/* LEXOGRAPHICALLY SORT SUFFIX ARRAYS (not strictly necessary) */    
		Collections.sort(dataSuffixList);
		Collections.sort(lineSuffixList);
		//System.out.println(dataSuffixList);
		//System.out.println(lineSuffixList);
		
		/* NOW COMPARE ARRAYS MEMBER BY MEMBER */
		String d = null;
		String l = null;
		String shorterTemp = null;
		int stopLength = 0;
		int k = 0;
		boolean match = false;
		
		bestMatch = new StringBuffer(currentMatch.toString());
		for(int i=0;i<dataSuffixList.size();i++)
		{
			d = dataSuffixList.get(i);
			for(int j=0;j<lineSuffixList.size();j++)
			{
				l = lineSuffixList.get(j);
				//System.out.println(d);
				//System.out.println(l);
				if(d.length()<l.length())
				{
					shorterTemp = d;
				}else{
					shorterTemp = l;
				}
				
				//potentially expensive, but safe
				currentMatch.delete(0,currentMatch.length());
				k=0;
				stopLength = shorterTemp.length();
				/** You can add the assert back in if you compile and run
				 *  the program with the appropriate flags to enable asserts
				 *  (jdk >=1.4)
				 */
				//assert(k<stopLength);
				
				match = (l.charAt(k)==d.charAt(k));
				while(k<stopLength && match)
				{               
					if(l.charAt(k)==d.charAt(k))
					{
						//System.out.println("matched");
						currentMatch.append(shorterTemp.charAt(k));
						k++;
					}else{
						match = false;
					}
				}
				//System.out.println("current match = "+currentMatch.toString());
				//System.out.println("best    match = "+bestMatch.toString());
				//got a longer match, so erase bestMatch and replace it.
				if(currentMatch.length()>bestMatch.length())
				{
					//potentially expensive, but safe
					bestMatch.delete(0,bestMatch.length());
					/* replace bestMatch with our current match, which is longer */
					bestMatch = new StringBuffer(currentMatch.toString());
				}
			}
		}
		return bestMatch.toString();
	}


	public static String getPackageName(String className) {
		int idx = className.lastIndexOf(".");
		if (idx<0) return "";
		else {
			return className.substring(0, idx);
		}
	}


	public static String getClassName(String className) {
		int idx = className.lastIndexOf(".");
		if (idx<0) return className;
		else {
			return className.substring(idx+1, className.length());
		}
	}


	//TODO merge with longestCommonSubString
	public static List<String> commonSubStrings(String left, String right, int minLen) {
		List<String> substrs = new ArrayList<String>();
		/* BEFORE WE ALLOCATE ANY DATA STORAGE, VALIDATE ARGS */
		if(null==left || "".equals(left))
			return substrs;
		if(null==right || "".equals(right))
			return substrs;      
//		if(left.equals(right))
//			return left;
		
		/* ALLOCATE VARIABLES WE'LL NEED FOR THE ROUTINE */
//		StringBuffer bestMatch = new StringBuffer(0);
		StringBuffer currentMatch = new StringBuffer(1024);
		ArrayList<String> dataSuffixList = new ArrayList<String>();
		ArrayList<String> lineSuffixList = new ArrayList<String>();
//		String shorter = null;
//		String longer = null; 
//		
//		if(left.length()<right.length())
//		{
//			shorter = left;
//			longer = right;
//		}else{
//			shorter = right;
//			longer = left;
//		}
//		
//		/* Using some builtin String methods, take a couple of shortcuts */ 
//		if(longer.startsWith(shorter))
//		{
//			return shorter;
//		}else if(longer.endsWith(shorter)){
//			return shorter;
//		}
		
		/* FIRST, COMPUTE SUFFIX ARRAYS */
		for(int i=0;i<left.length();i++)
		{
			dataSuffixList.add(left.substring(i,left.length()));
		}
		for(int i=0;i<right.length();i++)
		{
			lineSuffixList.add(right.substring(i,right.length()));
		}
		
		/* LEXOGRAPHICALLY SORT SUFFIX ARRAYS (not strictly necessary) */    
		Collections.sort(dataSuffixList);
		Collections.sort(lineSuffixList);
		//System.out.println(dataSuffixList);
		//System.out.println(lineSuffixList);
		
		/* NOW COMPARE ARRAYS MEMBER BY MEMBER */
		String d = null;
		String l = null;
		String shorterTemp = null;
		int stopLength = 0;
		int k = 0;
		boolean match = false;
		
//		bestMatch = new StringBuffer(currentMatch.toString());
		for(int i=0;i<dataSuffixList.size();i++)
		{
			d = dataSuffixList.get(i);
			for(int j=0;j<lineSuffixList.size();j++)
			{
				l = lineSuffixList.get(j);
				//System.out.println(d);
				//System.out.println(l);
				if(d.length()<l.length())
				{
					shorterTemp = d;
				}else{
					shorterTemp = l;
				}
				
				//potentially expensive, but safe
				currentMatch.delete(0,currentMatch.length());
				k=0;
				stopLength = shorterTemp.length();
				/** You can add the assert back in if you compile and run
				 *  the program with the appropriate flags to enable asserts
				 *  (jdk >=1.4)
				 */
				//assert(k<stopLength);
				
				match = (l.charAt(k)==d.charAt(k));
				while(k<stopLength && match)
				{               
					if(l.charAt(k)==d.charAt(k))
					{
						//System.out.println("matched");
						currentMatch.append(shorterTemp.charAt(k));
						k++;
					}else{
						match = false;
					}
				}
				//System.out.println("current match = "+currentMatch.toString());
				//System.out.println("best    match = "+bestMatch.toString());
				//got a longer match, so erase bestMatch and replace it.
//				if(currentMatch.length()>bestMatch.length())
				if(currentMatch.length()>=minLen)
				{
					substrs.add(currentMatch.toString());
				}
			}
		}
		return substrs;
	}


	//TODO this method is not done at all!!!
	//TODO only considers * wildcard so far
	public static List <String> commonMatchingPatterns(final String str1, final String str2, int minLenForEachKeyword) {
		List<String> results = new ArrayList<String>(); 
		List<String> substrs = commonSubStrings(str1, str2, minLenForEachKeyword);
		Collections.sort(substrs, new Comparator<String>() {
			public int compare(String s1, String s2) {
				int i1 = str1.indexOf(s1);
				int i2 = str1.indexOf(s2);
				return i1==i2 ? 0 : (i1<i2 ? -1 : 1);
			}
		});
		
		//remove overlapping ones for str1
		int i=0, j=1;
		while (i<substrs.size() && j<substrs.size()) {
			int idxi = str1.indexOf(substrs.get(i));
			int idxj = str1.indexOf(substrs.get(j));
			if (idxj-idxi<substrs.get(i).length()) {
				substrs.remove(j);
			} else {
				i=j; j++;
			}
		}
		
		return results;
	}


	public static String aspectjPattern2Regexp(String pattern) {
		String regPattern = pattern;
		regPattern = regPattern.replaceAll("\\.", "\\\\."); // . => \.
		regPattern = regPattern.replaceAll("\\*", "\\[\\^\\\\.\\]\\*"); // * => [^\.]*
		regPattern = regPattern.replaceAll("\\\\.\\\\.", "\\[\\\\.\\[\\^\\\\.\\]\\+\\]\\*\\\\."); // \.\. => [\.[^\.]+]*\. 
		return regPattern;
	}


	/**
	 * @param string
	 * @return all words in a string based on camel case naming convention, e.g. for
	 *   setBorderColor, it will return set, Border, Color 
	 */
	public static String[] getAllWords(String string) {
		string = string.trim();
		List<String> results = new ArrayList<String>();
		String substr = "";
		for (int i=0; i<string.length(); i++) {
			char ch = string.charAt(i);
			if (!(ch<='z' && ch>='a') && substr.length()>0) {
				results.add(substr);
				substr="";
			}
			substr+=ch;
		}
		if (substr.length()>0) results.add(substr);
		return results.toArray(new String[results.size()]);
	}


	public static String convertToSigature(String packageName, String name) {
		// TODO should use standard java facility instead
		if (packageName.length()>0) packageName+=".";
		String fullName = packageName+name;
		String result = "L"+fullName.replaceAll("\\.", "\\/")+";";
		return result;
	}


	public static boolean isWhiteSpace(char c) {
		return Character.isWhitespace(c);
	}

	public static String join(String delim, Object... os) {
		String s = "";
		for(Object o:os) {
			if (s.length()>0) s+=delim;
			String oos = o.toString();
			if (o instanceof Collection)
				oos = join(delim, ((Collection<Object>)o).toArray());
			s+=oos;
		}
		return s;
	}	
}
