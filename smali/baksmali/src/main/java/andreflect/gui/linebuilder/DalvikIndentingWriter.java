/*
* Copyright (C) 2012 Sony Ericsson Mobile Communications AB
*
* This file is part of ApkAnalyzer.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package andreflect.gui.linebuilder;

import org.jf.dexlib.Code.Instruction;

/*
 * This coupling class is used for adding highlighting dalvik syntax
 * The new writer for disassembler implements this class and inherited from IndentingWriter 
 * see andreflect.gui.linebuilder.DalvikIndentingWriterImpl in ApkAnalyzer project
 */

public interface DalvikIndentingWriter {
	public static final int COLOR_REG=0x00880000;
	public static final int COLOR_STRING = 0x00005555;
	public static final int COLOR_REF = 0x000000FF;
	public static final int COLOR_SYMBOL = 0x00B00000;
	public static final int COLOR_SOURCELINE = 0x00AAAAAA;
	public static final int COLOR_CODEADDRESS = 0x00AAAAAA;

	public static final int COLOR_KEYWORD = 0x880088;
	public static final int COLOR_TEXT = 0x000000;
	public static final int COLOR_STATIC = 0x0000bb;

	public static final int COLOR_HEX = 0x008800;
	public static final int COLOR_PC = 0x888888;
	public static final int COLOR_OPCODE = 0x000000;
	public static final int COLOR_LABEL = 0x000088;
	public static final int COLOR_COMMENT = 0x888800;

	public static final int COLOR_ERROR = 0xff0000;

	public void setColor(int colorComment);

	public void resetColor();

	public void setLine(int line);

	public void setInsAddress(int codeAddress);
	
	public void setInstruction(Instruction instruction);
	
	public String getProtoString(String typeDescriptor);
}
