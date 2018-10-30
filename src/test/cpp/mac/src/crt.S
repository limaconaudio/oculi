.globl _start
_start:

#define r_type_insn(_f7, _rs2, _rs1, _f3, _rd, _opc) \
.word (((_f7) << 25) | ((_rs2) << 20) | ((_rs1) << 15) | ((_f3) << 12) | ((_rd) << 7) | ((_opc) << 0))

#define MAC_ACCUMULATOR_ACCESS(_rd ) \
r_type_insn(0b0000000, 0, 0, 0b000, _rd, 0b0001011)

#define MAC_PUSH( _rs1, _rs2 ) \
r_type_insn(0b0000001, _rs2, _rs1, 0b000, 0, 0b0001011)


//Init
    MAC_ACCUMULATOR_ACCESS(1)

//Test 1
    li x28, 1
    MAC_ACCUMULATOR_ACCESS(1)
    bne x1, x0, fail

//Test 2
    li x28, 2
    li x29, 0x00010000
    li x1, 0x00010000
    li x2, 0x00010000
    MAC_PUSH(1, 2)
    MAC_ACCUMULATOR_ACCESS(1)
    bne x1, x29, fail


//Test 3
    li x28, 3
    li x29, 0x00000010
    li x1, 0x00001000
    li x2, 0x00000100
    MAC_PUSH(1, 2)
    MAC_ACCUMULATOR_ACCESS(1)
    bne x1, x29, fail


//Test 4
    li x28, 4
    li x29, 0x00080000
    li x1, 0x00020000
    li x2, 0x00040000
    MAC_PUSH(1, 2)
    MAC_ACCUMULATOR_ACCESS(1)
    bne x1, x29, fail


//Test 5
    li x28, 5
    li x29, 0x00058000
    li x1, 0x00010000
    li x2, 0x00010000
    li x3, 0x00020000
    li x4, 0x00020000
    li x5, 0x00008000
    li x6, 0x00010000
    MAC_PUSH(1, 2)
    MAC_PUSH(3, 4)
    MAC_PUSH(5, 6)
    MAC_ACCUMULATOR_ACCESS(1)
    bne x1, x29, fail


    j pass

fail: //x28 => error code
    li x2, 0xF00FFF24
    sw x28, 0(x2)

pass:
    li x2, 0xF00FFF20
    sw x0, 0(x2)



    nop
    nop
    nop
    nop
    nop
    nop
