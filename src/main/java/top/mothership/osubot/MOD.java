package top.mothership.osubot;

public enum MOD {
    NONE(0),NF(1),EZ(2),HD(3),NV(4),HR(5),SD(6),DT(7),RL(8),HT(9),NC(10),FL(11),Auto(12),SO(13),AP(14),PF(15);

    MOD(int i) {
        this.i = i;
    }

    private int i;

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }


}
