package dev.undesarrolladormas.ensamblador.persistence;

public enum Theme {
    LIGHT("Light","com.formdev.flatlaf.themes.FlatMacLightLaf"),
    DARK("Dark", "com.formdev.flatlaf.themes.FlatMacDarkLaf"),
    MATERIAL_OCEAN("Material Deep Ocean", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDeepOceanIJTheme");

    private final String name;
    private final String longName;

    Theme(final String name, final String longName) {
        this.name = name;
        this.longName = longName;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getLongName() {
        return longName;
    }
}
