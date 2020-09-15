package com.camoga.emu;

import static com.camoga.emu.Chip8.raster;
import static com.camoga.emu.Chip8.PC;
import static com.camoga.emu.Chip8.reg;
import static com.camoga.emu.Chip8.I;
import static com.camoga.emu.Chip8.stack;
import static com.camoga.emu.Chip8.sp;
import static com.camoga.emu.Chip8.rand;
import static com.camoga.emu.Chip8.keys;
import static com.camoga.emu.Chip8.memory;

public class Opcodes {

	public static void clear() {
		for(int i = 0; i < raster.length; i++) {
			raster[i] = 0;
		}
		PC+=2;
	}
	
	public static void ret() {
		PC = stack[--sp];
	}
	
	public static void sys(byte hb, byte lb) {
		PC = (short) ((((hb&0x0F)<<8) | lb)<<1);
	}
	
	public static void jp(byte hb, byte lb) {
		PC = (short) ((((hb&0x0F)<<8) | lb) <<1);
	}
	
	public static void call(byte hb, byte lb) {
		stack[sp++] = (short) (PC+2);
		PC = (short) ((((hb&0x0F)<<8) | lb) <<1);
	}
	
	public static void se(byte hb, byte lb) {
		if(reg[hb&0x0F] == lb) PC += 4;
		else PC+=2;
	}
	
	public static void sne(byte hb, byte lb) {
		if(reg[hb&0x0F] != lb) PC += 4;
		else PC+=2;
	}

	public static void ser(byte hb, byte lb) {
		if(reg[hb&0x0F] == reg[(lb&0xF0) >> 4]) PC += 4;
		else PC+=2;
	}

	public static void ld(byte hb, byte lb) {
		reg[hb&0x0F] = lb;
		PC+=2;
	}

	public static void add(byte hb, byte lb) {
		reg[hb&0x0F] += lb;
		PC+=2;
	}
	
	public static void ldr(byte hb, byte lb) {
		reg[hb&0x0F] = reg[(lb&0xF0) >> 4];
		PC+=2;
	}
	
	public static void or(byte hb, byte lb) {
		int x = hb&0x0F;
		int y = (lb&0xF0)>>4;
		reg[x] |= reg[y];
		PC+=2;
	}
	
	public static void and(byte hb, byte lb) {
		int x = hb&0x0F;
		int y = (lb&0xF0)>>4;
		reg[x] &= reg[y];
		PC+=2;
	}
	
	public static void xor(byte hb, byte lb) {
		int x = hb&0x0F;
		int y = (lb&0xF0)>>4;
		reg[x] ^= reg[y];
		PC+=2;
	}
	
	public static void addr(byte hb, byte lb) {
		int x = hb&0x0F;
		int y = (lb&0xF0)>>4;
		int sum = (reg[x]&0xff)+(reg[y]&0xff);
		reg[0xf] = (byte) (sum > 255 ? 1:0);
		reg[x] = (byte) sum;
		PC+=2;
	}
	
	public static void subr(byte hb, byte lb) {
		int x = hb&0x0F;
		int y = (lb&0xF0)>>4;
		if(reg[x] > reg[y]) reg[0xf] = 1;
		else reg[0xf] = 0;
		reg[x] = (byte) (reg[x]-reg[y]);
		PC+=2;
	}
	
	public static void shr(byte hb) {
		int x = hb&0x0F;
		reg[0xf] = (byte) (reg[x]&1);
		reg[x] >>= 1;
		PC+=2;
	}
	
	public static void subn(byte hb, byte lb) {
		int x = hb&0x0F;
		int y = (lb&0xF0)>>4;
		if(reg[y] > reg[x]) reg[0xf] = 1;
		reg[x] = (byte) (reg[y]-reg[x]);
		PC+=2;
	}
	
	public static void shl(byte hb) {
		int x = hb&0x0F;
		reg[0xf] = (byte) (reg[x]&0b10000000);
		reg[x] <<= 1;
		PC+=2;
	}
	
	public static void prnt(byte hb) {
//		System.out.println(reg[hb&0x0F]&0xff);
		PC+=2;
	}
	
	public static void sner(byte hb, byte lb) {
		if(reg[hb&0x0F] != reg[(lb&0xF0) >> 4]) PC += 4;
		else PC+=2;
	}
	
	public static void ldi(byte hb, byte lb) {
		I = (short) (((hb&0x0F)<<8) | lb);
		PC+=2;
	}
	
	public static void jps(byte hb, byte lb) {
		PC += (short) ((((hb&0x0F)<<8) | lb)<<1);
	}
	
	public static void rnd(byte hb, byte lb) {
		reg[hb&0x0F] = (byte) (rand.nextInt(256)&lb);
		PC+=2;
	}
	
	public static void drw(byte hb, byte lb) {
		int x = reg[hb&0x0F];
		int y = reg[(lb&0xF0)>>4];
		int rows = lb&0x0F;
		reg[0xf] = 0;
		for(int i = 0; i < rows; i++, y++) {
			int row = memory[I++]&0xff;
//			System.out.println(row);
			for(int j = 7; j >= 0; j--) {
				int index = (x+j)&0x3f | (y<<6);
				int value = raster[index];
				raster[index] ^= row&1;
				if(value == 1 && raster[index] == 0) reg[0xf] = 1;
				row >>= 1;
			}
		}
		
		PC+=2;
	}
	
	public static void skp(byte hb, byte lb) {
		if(keys[reg[hb&0x0F]]) PC+=4;
		else PC+=2;
	}
	
	public static void sknp(byte hb, byte lb) {
		if(keys[reg[hb&0x0F]]) PC+=2;
		else PC+=4;
	}
	
	public static void ldvdt(byte hb) {
		reg[hb&0x0F] = delaytimer;
		PC+=2;
	}
	
	public static void ldk(byte hb) {
		//TODO
	}
	
	public static void setdt(byte hb) {
		delaytimer = reg[hb&0x0F];
		PC+=2;
	}
	
	public static void setst(byte hb) {
		soundtimer = reg[hb&0x0F];
		PC+=2;
	}
	
	public static void addi(byte hb) {
		I += reg[hb&0x0F];
		PC+=2;
	}
	
	public static void ldhex(byte hb) {
		I = (short) ((reg[hb&0x0F]&0xF) *5);
		PC+=2;
	}
	
	public static void lddec(byte hb) {
		PC+=2;
	}
	
	public static void streg(byte hb) {
		int x = hb&0x0F;
		for(int i = 0; i <= x; i++) {
			memory[I++] = reg[i];
		}
		PC+=2;
	}

	public static void ldreg(byte hb) {
		int x = hb&0x0F;
		for(int i = 0; i <= x; i++) {
			reg[i] = memory[I++];
		}
		PC+=2;
	}
}
