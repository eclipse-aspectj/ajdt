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
package org.eclipse.ajdt.pointcutdoctor.core.explain;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExplainMessageProcessor {
	
	public static class EnumDecl {
		public String code;
		public String message;
		EnumDecl(String code, String message) {
			this.code = code;
			this.message = message;
		}
		public static EnumDecl parse(String line) {
			String regex = "\\s*\\-\\s\\[([MSG|M-MSG][^\\[\\]]*)\\]\\s*(.*)";
			Pattern pattern = Pattern.compile(regex);
			// parse the line
			Matcher m = pattern.matcher(line);
			if (m.matches()) {
				String code = m.group(1);
				String msg = m.group(2);
				return new EnumDecl(code, msg); 
			} else return null;
		}
		
		public String getMessageForJava() {
			return message.replaceAll("\"", "\\\\\"");
		}
	}
	
	private List<EnumDecl> messages = new ArrayList<EnumDecl>();

	public ExplainMessageProcessor(File msgFile) throws IOException {
		readMessages(msgFile);
	}

	private void readMessages(File msgFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(msgFile));
		String line;
		String[] prefixes = {"- [MSG", "- [M_MSG"};
		messages.clear();
		Set<String> addedCodes = new HashSet<String>();
		while((line=reader.readLine())!=null) {
			line = line.trim();
			boolean isMsgLine = false;
			for (String pre:prefixes)
				if (line.startsWith(pre)) {
					isMsgLine = true; break;
				}
			EnumDecl decl = EnumDecl.parse(line);
			if (isMsgLine && !addedCodes.contains(decl.code)) { 
				messages.add(decl);
				addedCodes.add(decl.code);
			}
		}
	}

	private void writeEnum(File targetFile) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile));
		
		List<String> lines = new ArrayList<String>();
		lines.add("package org.eclipse.ajdt.pointcutdoctor.core.explain;");
		lines.add("");
		lines.add("public enum ExplainMessage {");
		
		for (int i=0; i<messages.size(); i++) {
			EnumDecl enm = messages.get(i);
			char period = i<messages.size()-1 ? ',' : ';';
			String msg = enm.getMessageForJava();
			lines.add(String.format("\t%s (\"%s\")%s", enm.code, msg, period));
		}
		
		lines.add("");	
		lines.add("\tprivate String message = \"\";");
		lines.add("\tExplainMessage(String msg) {");
		lines.add("		message = msg;");
		lines.add("	}");
		lines.add("	public String getMessage() {");
		lines.add("		String regex = \"\\\\[[^\\\\[\\\\]]*\\\\]\";");
		lines.add("		return message.replaceAll(regex, \"%s\");");
		lines.add("	}");
		lines.add("}");
		
		for (String line:lines) {
			writer.write(line); writer.write("\n");
		}
		
		writer.close();
	}

	public static void main(String[] args) {
		File sourceFile = new File(args[0]);
		File targetFile = new File(args[1]);
		ExplainMessageProcessor processor;
		try {
			processor = new ExplainMessageProcessor(sourceFile);
			processor.writeEnum(targetFile);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(String.format("SourceFile: %s, TargetFile: %s",
					sourceFile, targetFile));
		}
	}

}
