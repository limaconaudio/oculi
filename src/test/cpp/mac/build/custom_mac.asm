
build/custom_mac.elf:     file format elf32-littleriscv


Disassembly of section .crt_section:

00000000 <_start>:
   0:	0000008b          	0x8b
   4:	00100e13          	li	t3,1
   8:	0000008b          	0x8b
   c:	08009863          	bnez	ra,9c <fail>
  10:	00200e13          	li	t3,2
  14:	00010eb7          	lui	t4,0x10
  18:	000100b7          	lui	ra,0x10
  1c:	00010137          	lui	sp,0x10
  20:	0220800b          	0x220800b
  24:	0000008b          	0x8b
  28:	07d09a63          	bne	ra,t4,9c <fail>
  2c:	00300e13          	li	t3,3
  30:	01000e93          	li	t4,16
  34:	000010b7          	lui	ra,0x1
  38:	10000113          	li	sp,256
  3c:	0220800b          	0x220800b
  40:	0000008b          	0x8b
  44:	05d09c63          	bne	ra,t4,9c <fail>
  48:	00400e13          	li	t3,4
  4c:	00080eb7          	lui	t4,0x80
  50:	000200b7          	lui	ra,0x20
  54:	00040137          	lui	sp,0x40
  58:	0220800b          	0x220800b
  5c:	0000008b          	0x8b
  60:	03d09e63          	bne	ra,t4,9c <fail>
  64:	00500e13          	li	t3,5
  68:	00058eb7          	lui	t4,0x58
  6c:	000100b7          	lui	ra,0x10
  70:	00010137          	lui	sp,0x10
  74:	000201b7          	lui	gp,0x20
  78:	00020237          	lui	tp,0x20
  7c:	000082b7          	lui	t0,0x8
  80:	00010337          	lui	t1,0x10
  84:	0220800b          	0x220800b
  88:	0241800b          	0x241800b
  8c:	0262800b          	0x262800b
  90:	0000008b          	0x8b
  94:	01d09463          	bne	ra,t4,9c <fail>
  98:	0100006f          	j	a8 <pass>

0000009c <fail>:
  9c:	f0100137          	lui	sp,0xf0100
  a0:	f2410113          	addi	sp,sp,-220 # f00fff24 <pass+0xf00ffe7c>
  a4:	01c12023          	sw	t3,0(sp)

000000a8 <pass>:
  a8:	f0100137          	lui	sp,0xf0100
  ac:	f2010113          	addi	sp,sp,-224 # f00fff20 <pass+0xf00ffe78>
  b0:	00012023          	sw	zero,0(sp)
  b4:	00000013          	nop
  b8:	00000013          	nop
  bc:	00000013          	nop
  c0:	00000013          	nop
  c4:	00000013          	nop
  c8:	00000013          	nop
