package org.aspectj.apache.bcel.generic;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import org.aspectj.apache.bcel.Constants;

/**
 * This interface contains shareable instruction objects.
 *
 * In order to save memory you can use some instructions multiply,
 * since they have an immutable state and are directly derived from
 * Instruction.  I.e. they have no instance fields that could be
 * changed. Since some of these instructions like ICONST_0 occur
 * very frequently this can save a lot of time and space. This
 * feature is an adaptation of the FlyWeight design pattern, we
 * just use an array instead of a factory.
 *
 * The Instructions can also accessed directly under their names, so
 * it's possible to write il.append(Instruction.ICONST_0);
 *
 * @version $Id: InstructionConstants.java,v 1.4 2008/08/13 18:18:22 aclement Exp $
 * @author  <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public interface InstructionConstants {
  /** Predefined instruction objects
   */
  Instruction           NOP          = new Instruction(Constants.NOP);
  Instruction           ACONST_NULL  = new Instruction(Constants.ACONST_NULL);
  Instruction           ICONST_M1    = new Instruction(Constants.ICONST_M1);
  Instruction           ICONST_0     = new Instruction(Constants.ICONST_0);
  Instruction           ICONST_1     = new Instruction(Constants.ICONST_1);
  Instruction           ICONST_2     = new Instruction(Constants.ICONST_2);
  Instruction           ICONST_3     = new Instruction(Constants.ICONST_3);
  Instruction           ICONST_4     = new Instruction(Constants.ICONST_4);
  Instruction           ICONST_5     = new Instruction(Constants.ICONST_5);
  Instruction           LCONST_0     = new Instruction(Constants.LCONST_0);
  Instruction           LCONST_1     = new Instruction(Constants.LCONST_1);
  Instruction           FCONST_0     = new Instruction(Constants.FCONST_0);
  Instruction           FCONST_1     = new Instruction(Constants.FCONST_1);
  Instruction           FCONST_2     = new Instruction(Constants.FCONST_2);
  Instruction           DCONST_0     = new Instruction(Constants.DCONST_0);
  Instruction           DCONST_1     = new Instruction(Constants.DCONST_1);
  Instruction      	  IALOAD       = new Instruction(Constants.IALOAD);
  Instruction      	  LALOAD       = new Instruction(Constants.LALOAD);
  Instruction      	  FALOAD       = new Instruction(Constants.FALOAD);
  Instruction      	  DALOAD       = new Instruction(Constants.DALOAD);
  Instruction      	  AALOAD       = new Instruction(Constants.AALOAD);
  Instruction      	  BALOAD       = new Instruction(Constants.BALOAD);
  Instruction      	  CALOAD       = new Instruction(Constants.CALOAD);
  Instruction      	  SALOAD       = new Instruction(Constants.SALOAD);
  Instruction      	  IASTORE      = new Instruction(Constants.IASTORE);
  Instruction      	  LASTORE      = new Instruction(Constants.LASTORE);
  Instruction      	  FASTORE      = new Instruction(Constants.FASTORE);
  Instruction      	  DASTORE      = new Instruction(Constants.DASTORE);
  Instruction      	  AASTORE      = new Instruction(Constants.AASTORE);
  Instruction      	  BASTORE      = new Instruction(Constants.BASTORE);
  Instruction      	  CASTORE      = new Instruction(Constants.CASTORE);
  Instruction      	  SASTORE      = new Instruction(Constants.SASTORE);
  Instruction      POP          = new Instruction(Constants.POP);
  Instruction      POP2         = new Instruction(Constants.POP2);
  Instruction      DUP          = new Instruction(Constants.DUP);
  Instruction      DUP_X1       = new Instruction(Constants.DUP_X1);
  Instruction      DUP_X2       = new Instruction(Constants.DUP_X2);
  Instruction      DUP2         = new Instruction(Constants.DUP2);
  Instruction      DUP2_X1      = new Instruction(Constants.DUP2_X1);
  Instruction      DUP2_X2      = new Instruction(Constants.DUP2_X2);
  Instruction      SWAP         = new Instruction(Constants.SWAP);
  Instruction IADD         = new Instruction(Constants.IADD);
  Instruction LADD         = new Instruction(Constants.LADD);
  Instruction FADD         = new Instruction(Constants.FADD);
  Instruction DADD         = new Instruction(Constants.DADD);
  Instruction ISUB         = new Instruction(Constants.ISUB);
  Instruction LSUB         = new Instruction(Constants.LSUB);
  Instruction FSUB         = new Instruction(Constants.FSUB);
  Instruction DSUB         = new Instruction(Constants.DSUB);
  Instruction IMUL         = new Instruction(Constants.IMUL);
  Instruction LMUL         = new Instruction(Constants.LMUL);
  Instruction FMUL         = new Instruction(Constants.FMUL);
  Instruction DMUL         = new Instruction(Constants.DMUL);
  Instruction IDIV         = new Instruction(Constants.IDIV);
  Instruction LDIV         = new Instruction(Constants.LDIV);
  Instruction FDIV         = new Instruction(Constants.FDIV);
  Instruction DDIV         = new Instruction(Constants.DDIV);
  Instruction IREM         = new Instruction(Constants.IREM);
  Instruction LREM         = new Instruction(Constants.LREM);
  Instruction FREM         = new Instruction(Constants.FREM);
  Instruction DREM         = new Instruction(Constants.DREM);
  Instruction INEG         = new Instruction(Constants.INEG);
  Instruction LNEG         = new Instruction(Constants.LNEG);
  Instruction FNEG         = new Instruction(Constants.FNEG);
  Instruction DNEG         = new Instruction(Constants.DNEG);
  Instruction ISHL         = new Instruction(Constants.ISHL);
  Instruction LSHL         = new Instruction(Constants.LSHL);
  Instruction ISHR         = new Instruction(Constants.ISHR);
  Instruction LSHR         = new Instruction(Constants.LSHR);
  Instruction IUSHR        = new Instruction(Constants.IUSHR);
  Instruction LUSHR        = new Instruction(Constants.LUSHR);
  Instruction IAND         = new Instruction(Constants.IAND);
  Instruction LAND         = new Instruction(Constants.LAND);
  Instruction IOR          = new Instruction(Constants.IOR);
  Instruction LOR          = new Instruction(Constants.LOR);
  Instruction IXOR         = new Instruction(Constants.IXOR);
  Instruction LXOR         = new Instruction(Constants.LXOR);
  Instruction I2L          = new Instruction(Constants.I2L);
  Instruction I2F          = new Instruction(Constants.I2F);
  Instruction I2D          = new Instruction(Constants.I2D);
  Instruction L2I          = new Instruction(Constants.L2I);
  Instruction L2F          = new Instruction(Constants.L2F);
  Instruction L2D          = new Instruction(Constants.L2D);
  Instruction F2I          = new Instruction(Constants.F2I);
  Instruction F2L          = new Instruction(Constants.F2L);
  Instruction F2D          = new Instruction(Constants.F2D);
  Instruction D2I          = new Instruction(Constants.D2I);
  Instruction D2L          = new Instruction(Constants.D2L);
  Instruction D2F          = new Instruction(Constants.D2F);
  Instruction I2B          = new Instruction(Constants.I2B);
  Instruction I2C          = new Instruction(Constants.I2C);
  Instruction I2S          = new Instruction(Constants.I2S);
  Instruction           LCMP         = new Instruction(Constants.LCMP);
  Instruction           FCMPL        = new Instruction(Constants.FCMPL);
  Instruction           FCMPG        = new Instruction(Constants.FCMPG);
  Instruction           DCMPL        = new Instruction(Constants.DCMPL);
  Instruction           DCMPG        = new Instruction(Constants.DCMPG);
  Instruction     IRETURN      = new Instruction(Constants.IRETURN);
  Instruction     LRETURN      = new Instruction(Constants.LRETURN);
  Instruction     FRETURN      = new Instruction(Constants.FRETURN);
  Instruction     DRETURN      = new Instruction(Constants.DRETURN);
  Instruction     ARETURN      = new Instruction(Constants.ARETURN);
  Instruction     RETURN       = new Instruction(Constants.RETURN);
  Instruction           ARRAYLENGTH  = new Instruction(Constants.ARRAYLENGTH);
  Instruction           ATHROW       = new Instruction(Constants.ATHROW);
  Instruction           MONITORENTER = new Instruction(Constants.MONITORENTER);
  Instruction           MONITOREXIT  = new Instruction(Constants.MONITOREXIT);
  Instruction           IMPDEP1  = new Instruction(Constants.IMPDEP1);
  Instruction           IMPDEP2  = new Instruction(Constants.IMPDEP2);

  // You can use these constants in multiple places safely, any attempt to change the index
  // for these constants will cause an exception
  InstructionLV THIS    = new InstructionCLV(Constants.ALOAD,0);
  InstructionLV ALOAD_0 = new InstructionCLV(Constants.ALOAD_0);
  InstructionLV ALOAD_1 = new InstructionCLV(Constants.ALOAD_1);
  InstructionLV ALOAD_2 = new InstructionCLV(Constants.ALOAD_2);
  InstructionLV ALOAD_3 = new InstructionCLV(Constants.ALOAD_3);
  InstructionLV ILOAD_0 = new InstructionCLV(Constants.ILOAD_0);
  InstructionLV ILOAD_1 = new InstructionCLV(Constants.ILOAD_1);
  InstructionLV ILOAD_2 = new InstructionCLV(Constants.ILOAD_2);
  InstructionLV ILOAD_3 = new InstructionCLV(Constants.ILOAD_3);
  InstructionLV DLOAD_0 = new InstructionCLV(Constants.DLOAD_0);
  InstructionLV DLOAD_1 = new InstructionCLV(Constants.DLOAD_1);
  InstructionLV DLOAD_2 = new InstructionCLV(Constants.DLOAD_2);
  InstructionLV DLOAD_3 = new InstructionCLV(Constants.DLOAD_3);
  InstructionLV FLOAD_0 = new InstructionCLV(Constants.FLOAD_0);
  InstructionLV FLOAD_1 = new InstructionCLV(Constants.FLOAD_1);
  InstructionLV FLOAD_2 = new InstructionCLV(Constants.FLOAD_2);
  InstructionLV FLOAD_3 = new InstructionCLV(Constants.FLOAD_3);
  InstructionLV LLOAD_0 = new InstructionCLV(Constants.LLOAD_0);
  InstructionLV LLOAD_1 = new InstructionCLV(Constants.LLOAD_1);
  InstructionLV LLOAD_2 = new InstructionCLV(Constants.LLOAD_2);
  InstructionLV LLOAD_3 = new InstructionCLV(Constants.LLOAD_3);
  InstructionLV ASTORE_0 = new InstructionCLV(Constants.ASTORE_0);
  InstructionLV ASTORE_1 = new InstructionCLV(Constants.ASTORE_1);
  InstructionLV ASTORE_2 = new InstructionCLV(Constants.ASTORE_2);
  InstructionLV ASTORE_3 = new InstructionCLV(Constants.ASTORE_3);
  InstructionLV ISTORE_0 = new InstructionCLV(Constants.ISTORE_0);
  InstructionLV ISTORE_1 = new InstructionCLV(Constants.ISTORE_1);
  InstructionLV ISTORE_2 = new InstructionCLV(Constants.ISTORE_2);
  InstructionLV ISTORE_3 = new InstructionCLV(Constants.ISTORE_3);
  InstructionLV LSTORE_0 = new InstructionCLV(Constants.LSTORE_0);
  InstructionLV LSTORE_1 = new InstructionCLV(Constants.LSTORE_1);
  InstructionLV LSTORE_2 = new InstructionCLV(Constants.LSTORE_2);
  InstructionLV LSTORE_3 = new InstructionCLV(Constants.LSTORE_3);
  InstructionLV FSTORE_0 = new InstructionCLV(Constants.FSTORE_0);
  InstructionLV FSTORE_1 = new InstructionCLV(Constants.FSTORE_1);
  InstructionLV FSTORE_2 = new InstructionCLV(Constants.FSTORE_2);
  InstructionLV FSTORE_3 = new InstructionCLV(Constants.FSTORE_3);
  InstructionLV DSTORE_0 = new InstructionCLV(Constants.DSTORE_0);
  InstructionLV DSTORE_1 = new InstructionCLV(Constants.DSTORE_1);
  InstructionLV DSTORE_2 = new InstructionCLV(Constants.DSTORE_2);
  InstructionLV DSTORE_3 = new InstructionCLV(Constants.DSTORE_3);


  /** Get object via its opcode, for immutable instructions like
   * branch instructions entries are set to null.
   */
  Instruction[] INSTRUCTIONS = new Instruction[256];

  /** Interfaces may have no static initializers, so we simulate this
   * with an inner class.
   */
  Clinit bla = new Clinit();

  class Clinit {
    Clinit() {
      INSTRUCTIONS[Constants.NOP] = NOP;
      INSTRUCTIONS[Constants.ACONST_NULL] = ACONST_NULL;
      INSTRUCTIONS[Constants.ICONST_M1] = ICONST_M1;
      INSTRUCTIONS[Constants.ICONST_0] = ICONST_0;
      INSTRUCTIONS[Constants.ICONST_1] = ICONST_1;
      INSTRUCTIONS[Constants.ICONST_2] = ICONST_2;
      INSTRUCTIONS[Constants.ICONST_3] = ICONST_3;
      INSTRUCTIONS[Constants.ICONST_4] = ICONST_4;
      INSTRUCTIONS[Constants.ICONST_5] = ICONST_5;
      INSTRUCTIONS[Constants.LCONST_0] = LCONST_0;
      INSTRUCTIONS[Constants.LCONST_1] = LCONST_1;
      INSTRUCTIONS[Constants.FCONST_0] = FCONST_0;
      INSTRUCTIONS[Constants.FCONST_1] = FCONST_1;
      INSTRUCTIONS[Constants.FCONST_2] = FCONST_2;
      INSTRUCTIONS[Constants.DCONST_0] = DCONST_0;
      INSTRUCTIONS[Constants.DCONST_1] = DCONST_1;
      INSTRUCTIONS[Constants.IALOAD] = IALOAD;
      INSTRUCTIONS[Constants.LALOAD] = LALOAD;
      INSTRUCTIONS[Constants.FALOAD] = FALOAD;
      INSTRUCTIONS[Constants.DALOAD] = DALOAD;
      INSTRUCTIONS[Constants.AALOAD] = AALOAD;
      INSTRUCTIONS[Constants.BALOAD] = BALOAD;
      INSTRUCTIONS[Constants.CALOAD] = CALOAD;
      INSTRUCTIONS[Constants.SALOAD] = SALOAD;
      INSTRUCTIONS[Constants.IASTORE] = IASTORE;
      INSTRUCTIONS[Constants.LASTORE] = LASTORE;
      INSTRUCTIONS[Constants.FASTORE] = FASTORE;
      INSTRUCTIONS[Constants.DASTORE] = DASTORE;
      INSTRUCTIONS[Constants.AASTORE] = AASTORE;
      INSTRUCTIONS[Constants.BASTORE] = BASTORE;
      INSTRUCTIONS[Constants.CASTORE] = CASTORE;
      INSTRUCTIONS[Constants.SASTORE] = SASTORE;
      INSTRUCTIONS[Constants.POP] = POP;
      INSTRUCTIONS[Constants.POP2] = POP2;
      INSTRUCTIONS[Constants.DUP] = DUP;
      INSTRUCTIONS[Constants.DUP_X1] = DUP_X1;
      INSTRUCTIONS[Constants.DUP_X2] = DUP_X2;
      INSTRUCTIONS[Constants.DUP2] = DUP2;
      INSTRUCTIONS[Constants.DUP2_X1] = DUP2_X1;
      INSTRUCTIONS[Constants.DUP2_X2] = DUP2_X2;
      INSTRUCTIONS[Constants.SWAP] = SWAP;
      INSTRUCTIONS[Constants.IADD] = IADD;
      INSTRUCTIONS[Constants.LADD] = LADD;
      INSTRUCTIONS[Constants.FADD] = FADD;
      INSTRUCTIONS[Constants.DADD] = DADD;
      INSTRUCTIONS[Constants.ISUB] = ISUB;
      INSTRUCTIONS[Constants.LSUB] = LSUB;
      INSTRUCTIONS[Constants.FSUB] = FSUB;
      INSTRUCTIONS[Constants.DSUB] = DSUB;
      INSTRUCTIONS[Constants.IMUL] = IMUL;
      INSTRUCTIONS[Constants.LMUL] = LMUL;
      INSTRUCTIONS[Constants.FMUL] = FMUL;
      INSTRUCTIONS[Constants.DMUL] = DMUL;
      INSTRUCTIONS[Constants.IDIV] = IDIV;
      INSTRUCTIONS[Constants.LDIV] = LDIV;
      INSTRUCTIONS[Constants.FDIV] = FDIV;
      INSTRUCTIONS[Constants.DDIV] = DDIV;
      INSTRUCTIONS[Constants.IREM] = IREM;
      INSTRUCTIONS[Constants.LREM] = LREM;
      INSTRUCTIONS[Constants.FREM] = FREM;
      INSTRUCTIONS[Constants.DREM] = DREM;
      INSTRUCTIONS[Constants.INEG] = INEG;
      INSTRUCTIONS[Constants.LNEG] = LNEG;
      INSTRUCTIONS[Constants.FNEG] = FNEG;
      INSTRUCTIONS[Constants.DNEG] = DNEG;
      INSTRUCTIONS[Constants.ISHL] = ISHL;
      INSTRUCTIONS[Constants.LSHL] = LSHL;
      INSTRUCTIONS[Constants.ISHR] = ISHR;
      INSTRUCTIONS[Constants.LSHR] = LSHR;
      INSTRUCTIONS[Constants.IUSHR] = IUSHR;
      INSTRUCTIONS[Constants.LUSHR] = LUSHR;
      INSTRUCTIONS[Constants.IAND] = IAND;
      INSTRUCTIONS[Constants.LAND] = LAND;
      INSTRUCTIONS[Constants.IOR] = IOR;
      INSTRUCTIONS[Constants.LOR] = LOR;
      INSTRUCTIONS[Constants.IXOR] = IXOR;
      INSTRUCTIONS[Constants.LXOR] = LXOR;
      INSTRUCTIONS[Constants.I2L] = I2L;
      INSTRUCTIONS[Constants.I2F] = I2F;
      INSTRUCTIONS[Constants.I2D] = I2D;
      INSTRUCTIONS[Constants.L2I] = L2I;
      INSTRUCTIONS[Constants.L2F] = L2F;
      INSTRUCTIONS[Constants.L2D] = L2D;
      INSTRUCTIONS[Constants.F2I] = F2I;
      INSTRUCTIONS[Constants.F2L] = F2L;
      INSTRUCTIONS[Constants.F2D] = F2D;
      INSTRUCTIONS[Constants.D2I] = D2I;
      INSTRUCTIONS[Constants.D2L] = D2L;
      INSTRUCTIONS[Constants.D2F] = D2F;
      INSTRUCTIONS[Constants.I2B] = I2B;
      INSTRUCTIONS[Constants.I2C] = I2C;
      INSTRUCTIONS[Constants.I2S] = I2S;
      INSTRUCTIONS[Constants.LCMP] = LCMP;
      INSTRUCTIONS[Constants.FCMPL] = FCMPL;
      INSTRUCTIONS[Constants.FCMPG] = FCMPG;
      INSTRUCTIONS[Constants.DCMPL] = DCMPL;
      INSTRUCTIONS[Constants.DCMPG] = DCMPG;
      INSTRUCTIONS[Constants.IRETURN] = IRETURN;
      INSTRUCTIONS[Constants.LRETURN] = LRETURN;
      INSTRUCTIONS[Constants.FRETURN] = FRETURN;
      INSTRUCTIONS[Constants.DRETURN] = DRETURN;
      INSTRUCTIONS[Constants.ARETURN] = ARETURN;
      INSTRUCTIONS[Constants.RETURN] = RETURN;
      INSTRUCTIONS[Constants.ARRAYLENGTH] = ARRAYLENGTH;
      INSTRUCTIONS[Constants.ATHROW] = ATHROW;
      INSTRUCTIONS[Constants.MONITORENTER] = MONITORENTER;
      INSTRUCTIONS[Constants.MONITOREXIT] = MONITOREXIT;
      INSTRUCTIONS[Constants.IMPDEP1] = IMPDEP1;
      INSTRUCTIONS[Constants.IMPDEP2] = IMPDEP2;

      INSTRUCTIONS[Constants.ALOAD_0] = ALOAD_0;INSTRUCTIONS[Constants.ALOAD_1] = ALOAD_1;
      INSTRUCTIONS[Constants.ALOAD_2] = ALOAD_2;INSTRUCTIONS[Constants.ALOAD_3] = ALOAD_3;
      INSTRUCTIONS[Constants.LLOAD_0] = LLOAD_0;INSTRUCTIONS[Constants.LLOAD_1] = LLOAD_1;
      INSTRUCTIONS[Constants.LLOAD_2] = LLOAD_2;INSTRUCTIONS[Constants.LLOAD_3] = LLOAD_3;
      INSTRUCTIONS[Constants.DLOAD_0] = DLOAD_0;INSTRUCTIONS[Constants.DLOAD_1] = DLOAD_1;
      INSTRUCTIONS[Constants.DLOAD_2] = DLOAD_2;INSTRUCTIONS[Constants.DLOAD_3] = DLOAD_3;
      INSTRUCTIONS[Constants.FLOAD_0] = FLOAD_0;INSTRUCTIONS[Constants.FLOAD_1] = FLOAD_1;
      INSTRUCTIONS[Constants.FLOAD_2] = FLOAD_2;INSTRUCTIONS[Constants.FLOAD_3] = FLOAD_3;
      INSTRUCTIONS[Constants.ILOAD_0] = ILOAD_0;INSTRUCTIONS[Constants.ILOAD_1] = ILOAD_1;
      INSTRUCTIONS[Constants.ILOAD_2] = ILOAD_2;INSTRUCTIONS[Constants.ILOAD_3] = ILOAD_3;

      INSTRUCTIONS[Constants.ASTORE_0] = ASTORE_0;INSTRUCTIONS[Constants.ASTORE_1] = ASTORE_1;
      INSTRUCTIONS[Constants.ASTORE_2] = ASTORE_2;INSTRUCTIONS[Constants.ASTORE_3] = ASTORE_3;
      INSTRUCTIONS[Constants.LSTORE_0] = LSTORE_0;INSTRUCTIONS[Constants.LSTORE_1] = LSTORE_1;
      INSTRUCTIONS[Constants.LSTORE_2] = LSTORE_2;INSTRUCTIONS[Constants.LSTORE_3] = LSTORE_3;
      INSTRUCTIONS[Constants.DSTORE_0] = DSTORE_0;INSTRUCTIONS[Constants.DSTORE_1] = DSTORE_1;
      INSTRUCTIONS[Constants.DSTORE_2] = DSTORE_2;INSTRUCTIONS[Constants.DSTORE_3] = DSTORE_3;
      INSTRUCTIONS[Constants.FSTORE_0] = FSTORE_0;INSTRUCTIONS[Constants.FSTORE_1] = FSTORE_1;
      INSTRUCTIONS[Constants.FSTORE_2] = FSTORE_2;INSTRUCTIONS[Constants.FSTORE_3] = FSTORE_3;
      INSTRUCTIONS[Constants.ISTORE_0] = ISTORE_0;INSTRUCTIONS[Constants.ISTORE_1] = ISTORE_1;
      INSTRUCTIONS[Constants.ISTORE_2] = ISTORE_2;INSTRUCTIONS[Constants.ISTORE_3] = ISTORE_3;
    }
  }
}
