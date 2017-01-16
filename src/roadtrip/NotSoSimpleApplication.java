package roadtrip;

import com.jme3.app.Application;
import com.jme3.app.DebugKeysAppState;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import com.jme3.system.JmeSystem;

/**
 * Cloned from SimpleApplication.
 *
 * Created by dejvino on 15.01.2017.
 */
public abstract class NotSoSimpleApplication extends Application
{    
    protected Node rootNode;
    protected Node guiNode;
    protected BitmapText fpsText;
    protected BitmapFont guiFont;
    protected boolean showSettings;
    private AppActionListener actionListener;

    public NotSoSimpleApplication(AppState... initialStates) {
        this.rootNode = new Node("Root Node");
        this.guiNode = new Node("Gui Node");
        this.showSettings = true;
        this.actionListener = new AppActionListener();
        attachStates(initialStates);
    }

    public final void attachStates(AppState... states)
    {
        if(states != null) {
            AppState[] arr$ = states;
            int len$ = states.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                AppState a = arr$[i$];
                if(a != null) {
                    this.stateManager.attach(a);
                }
            }
        }
    }

    public void attachDebugStates()
    {
        attachStates(new StatsAppState(guiNode, guiFont), new DebugKeysAppState());
    }

    @Override
    public void start() {
        boolean loadSettings = false;
        if(this.settings == null) {
            this.setSettings(new AppSettings(true));
            loadSettings = true;
        }

        if(!this.showSettings || JmeSystem.showSettingsDialog(this.settings, loadSettings)) {
            this.setSettings(this.settings);
            super.start();
        }
    }

    public Node getGuiNode() {
        return this.guiNode;
    }

    public Node getRootNode() {
        return this.rootNode;
    }

    public boolean isShowSettings() {
        return this.showSettings;
    }

    public void setShowSettings(boolean showSettings) {
        this.showSettings = showSettings;
    }

    protected BitmapFont loadGuiFont() {
        return this.assetManager.loadFont("Interface/Fonts/Default.fnt");
    }

    @Override
    public void initialize() {
        super.initialize();
        this.guiFont = this.loadGuiFont();
        this.guiNode.setQueueBucket(RenderQueue.Bucket.Gui);
        this.guiNode.setCullHint(Spatial.CullHint.Never);
        this.viewPort.attachScene(this.rootNode);
        this.guiViewPort.attachScene(this.guiNode);
        if(this.inputManager != null) {
            if(this.context.getType() == JmeContext.Type.Display) {
                this.inputManager.addMapping("SIMPLEAPP_Exit", new Trigger[]{new KeyTrigger(1)});
            }

            if(this.stateManager.getState(StatsAppState.class) != null) {
                this.inputManager.addMapping("SIMPLEAPP_HideStats", new Trigger[]{new KeyTrigger(63)});
                this.inputManager.addListener(this.actionListener, new String[]{"SIMPLEAPP_HideStats"});
            }

            this.inputManager.addListener(this.actionListener, new String[]{"SIMPLEAPP_Exit"});
        }

        if(this.stateManager.getState(StatsAppState.class) != null) {
            StatsAppState sas = (StatsAppState)this.stateManager.getState(StatsAppState.class);
            sas.setFont(this.guiFont);
            this.fpsText = sas.getFpsText();
        }

        this.initializeGame();
    }

    @Override
    public void update() {
        super.update();
        if (this.speed > 0.0f && !this.paused) {
            float tpf = this.timer.getTimePerFrame() * this.speed;
            updateStates(tpf);
            updateGame(tpf);
            updateLogicalState(tpf);
            updateGeometricState();
            updatePreRender(tpf);
            updateRender(tpf);
            updatePostRender(this.renderManager);
            updateStatesPostRender();
        }
    }

    protected void updateStates(float tpf) {
        this.stateManager.update(tpf);
    }

    public void updateGame(float tpf) {
    }

    protected void updateLogicalState(float tpf) {
        this.rootNode.updateLogicalState(tpf);
        this.guiNode.updateLogicalState(tpf);
    }

    protected void updateGeometricState() {
        this.rootNode.updateGeometricState();
        this.guiNode.updateGeometricState();
    }

    protected void updatePreRender(float tpf) {
    }

    protected void updateRender(float tpf) {
        this.stateManager.render(this.renderManager);
        this.renderManager.render(tpf, this.context.isRenderable());
    }

    public void updatePostRender(RenderManager rm) {
    }

    protected void updateStatesPostRender() {
        this.stateManager.postRender();
    }

    public void setDisplayFps(boolean show) {
        if(this.stateManager.getState(StatsAppState.class) != null) {
            ((StatsAppState)this.stateManager.getState(StatsAppState.class)).setDisplayFps(show);
        }

    }

    public void setDisplayStatView(boolean show) {
        if(this.stateManager.getState(StatsAppState.class) != null) {
            ((StatsAppState)this.stateManager.getState(StatsAppState.class)).setDisplayStatView(show);
        }

    }

    public abstract void initializeGame();

    private class AppActionListener implements ActionListener {
        private AppActionListener() {
        }

        public void onAction(String name, boolean value, float tpf) {
            if(value) {
                if(name.equals("SIMPLEAPP_Exit")) {
                    NotSoSimpleApplication.this.stop();
                } else if(name.equals("SIMPLEAPP_HideStats") && NotSoSimpleApplication.this.stateManager.getState(StatsAppState.class) != null) {
                    ((StatsAppState) NotSoSimpleApplication.this.stateManager.getState(StatsAppState.class)).toggleStats();
                }

            }
        }
    }
}

