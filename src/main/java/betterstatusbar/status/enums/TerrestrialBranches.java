package betterstatusbar.status.enums;

public enum TerrestrialBranches {
    子("鼠"), 丑("牛"), 寅("虎"), 卯("兔"), 辰("龙"), 巳("蛇"), 午("马"), 未("羊"), 申("猴"), 酉("鸡"), 戌("狗"), 亥("猪");

    private String animal;

    TerrestrialBranches(String animal) {
        this.animal = animal;
    }

    public String getAnimal() {
        return animal;
    }
}
