package com.camoga.emu;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

import javax.swing.JFrame;

public class Chip8 {
	
	static byte[] reg = new byte[16];
	static short I = 0;
	static byte[] memory = new byte[0x2000];
	static short PC = 0x200;
	
	static short[] stack = new short[256];
	static byte sp = 0;
	
	static Random rand = new Random(27);
	
	static boolean[] keys = new boolean[16];
	
	static BufferedImage image = new BufferedImage(64, 32, BufferedImage.TYPE_BYTE_GRAY);
	static byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	static byte[] raster = new byte[pixels.length];
	
	
	Canvas c = new Canvas();
	
	public Chip8() {
		JFrame frame = new JFrame("Chip8 Emulator");
		frame.setSize(image.getWidth()*10+40, image.getHeight()*10+40);
		frame.add(c);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		frame.setVisible(true);
		c.createBufferStrategy(3);
		c.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				
			}
			
			public void toggle(int key, boolean state) {
				switch(key) {
				case KeyEvent.VK_X:
					keys[0] = state;
					break;
				case KeyEvent.VK_1:
					keys[1] = state;
					break;
				case KeyEvent.VK_2:
					keys[2] = state;
					break;
				case KeyEvent.VK_3:
					keys[3] = state;
					break;
				case KeyEvent.VK_Q:
					keys[4] = state;
					break;
				case KeyEvent.VK_W:
					keys[5] = state;
					break;
				case KeyEvent.VK_E:
					keys[6] = state;
					break;
				case KeyEvent.VK_A:
					keys[7] = state;
					break;
				case KeyEvent.VK_S:
					keys[8] = state;
					break;
				case KeyEvent.VK_D:
					keys[9] = state;
					break;
				case KeyEvent.VK_Z:
					keys[10] = state;
					break;
				case KeyEvent.VK_C:
					keys[11] = state;
					break;
				case KeyEvent.VK_4:
					keys[12] = state;
					break;
				case KeyEvent.VK_R:
					keys[13] = state;
					break;
				case KeyEvent.VK_F:
					keys[14] = state;
					break;
				case KeyEvent.VK_V:
					keys[15] = state;
					break;
				}
			}
			
			public void keyReleased(KeyEvent e) {
				toggle(e.getKeyCode(), false);
			}
			
			public void keyPressed(KeyEvent e) {
				toggle(e.getKeyCode(), true);
			}
		});
		
		loadMem("/memory.hex");
		
		
		new Thread(() -> {
			while(true) {
				render();
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, "Render").start();
		
		int[] program = new int[] {
				0x60,0x01,
				0x61,0x01, 
				0x63, 0xf0, 
				0x64, 0x0f, 
				0x67, 0x09, 
				0x00, 0xe0, 
				0x85, 0x00, 
				0x86, 0x00, 
				0x82, 0x00, 
				0x85, 0x32, 
				0x86, 0x42, 
				0xf5, 0x29, 
				0xd8, 0x85, 
				0xf6, 0x29, 
				0xd7, 0x85,
				0x82,0x00,
				0x80,0x14, 
				0x81,0x20, 
				0x3f, 0x01, 
				0x11,0x05,
				0x11,0x00};
		loadProgram(program);
		run();
	}
	
	public void loadProgram(int[] program) {
		for(int i = 0, j = 0x200; i < program.length; i++, j++) {
			memory[j] = (byte) program[i];
		}
	}
	
	public void loadMem(String path) {
		BufferedInputStream bis = new BufferedInputStream(getClass().getResourceAsStream(path));
		try {
			bis.read(memory);
			bis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		long last = System.nanoTime();
		int i = 0;
		while(System.nanoTime() - last < 1e10) {
			byte hb = memory[PC];
			byte lb = memory[PC|1];
			executeOp(hb, lb);
//			System.out.format("%d\t%s\t%d\n", PC>>1, Integer.toHexString(((hb&0xff)<<8)| (lb&0xff)),reg[0xf]);
			i++;
//			try {
//				Thread.sleep(10);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		System.out.println("Steps = " + i);
	}
	
	public void executeOp(byte hb, byte lb) {
		switch(hb&0xF0) {
		case 0x00:
			if(hb==0x00) {
				System.out.println(Integer.toHexString(lb&0xf0));
				if(lb==-32) {
					Opcodes.clear();
				}
				else if(lb==0xEE) Opcodes.ret();
			}
			else Opcodes.sys(hb,lb);
			break;
		case 0x10:
			Opcodes.jp(hb,lb);
			break;
		case 0x20:
			Opcodes.call(hb,lb);
			break;
		case 0x30:
			Opcodes.se(hb,lb);
			break;
		case 0x40:
			Opcodes.sne(hb,lb);
			break;
		case 0x50:
			Opcodes.ser(hb,lb);
			break;
		case 0x60:
			Opcodes.ld(hb,lb);
			break;
		case 0x70:
			Opcodes.add(hb,lb);
			break;
		case 0x80:
			switch(lb&0xF) {
			case 0:
				Opcodes.ldr(hb,lb);
				break;
			case 1:
				Opcodes.or(hb,lb);
				break;
			case 2:
				Opcodes.and(hb,lb);
				break;
			case 3:
				Opcodes.xor(hb,lb);
				break;
			case 4:
				Opcodes.addr(hb,lb);
				break;
			case 5:
				Opcodes.subr(hb,lb);
				break;
			case 6:
				Opcodes.shr(hb);
				break;
			case 7:
				Opcodes.subn(hb,lb);
				break;
			case 8:
				Opcodes.prnt(hb);
				break;
			case 0xE:
				Opcodes.shl(hb);
				break;
			}
			break;
		case 0x90:
			Opcodes.sner(hb,lb);
			break;
		case 0xA0:
			Opcodes.ldi(hb,lb);
			break;
		case 0xB0:
			Opcodes.jps(hb,lb);
			break;
		case 0xC0:
			Opcodes.rnd(hb,lb);
			break;
		case 0xD0:
			Opcodes.drw(hb,lb);
			break;
		case 0xE0:
			switch(lb) {
			case (byte) 0x9E:
				Opcodes.skp(hb,lb);
				break;
			case (byte) 0xA1:
				Opcodes.sknp(hb,lb);
				break;
			}
			break;
		case 0xF0:
			switch(lb) {
			case 0x07:
				Opcodes.ldvdt(hb);
				break;
			case 0x0A:
				Opcodes.ldk(hb);
				break;
			case 0x15:
				Opcodes.setdt(hb);
				break;
			case 0x18:
				Opcodes.setst(hb);
				break;
			case 0x1E:
				Opcodes.addi(hb);
				break;
			case 0x29:
				Opcodes.ldhex(hb);
				break;
			case 0x33:
				Opcodes.lddec(hb);
				break;
			case 0x55:
				Opcodes.streg(hb);
			case 0x65:
				Opcodes.ldreg(hb);
			}
		}
			return;
	}
	
	public void render() {	
		for(int i = 0; i < pixels.length; i++) {
			pixels[i] = (byte) (0xff*raster[i]);
		}
		
		Graphics g = c.getBufferStrategy().getDrawGraphics();
		g.drawImage(image, 0, 0, image.getWidth()*10, image.getHeight()*10, null);
		g.dispose();
		c.getBufferStrategy().show();
	}
	
	public static void main(String[] args) {
		new Chip8();
	}
}