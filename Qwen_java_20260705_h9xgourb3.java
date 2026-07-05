package com.utilitymod.module;

import com.utilitymod.module.impl.*;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {

    private static final ModuleManager INSTANCE = new ModuleManager();
    private final List<Module> modules = new ArrayList<>();

    public static ModuleManager getInstance() {
        return INSTANCE;
    }

    public void registerAll() {
        register(new HUDModule());
        register(new VisualOverlayModule());
        register(new LegitAutoTotemModule());
        register(new SafeAnchorModule());
        register(new AutoCrystalModule());
    }

    public void register(Module module) {
        modules.add(module);
    }

    public void pollKeybinds() {
        for (Module m : modules) {
            m.pollKey();
        }
    }

    public void tickModules() {
        for (Module m : modules) {
            if (m.isEnabled()) {
                m.tick();
            }
        }
    }

    public List<Module> getModules() {
        return modules;
    }

    public Module getByName(String name) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }
}