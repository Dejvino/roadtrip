package roadtrip;

import com.jme3.app.state.AppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;

/**
 * Created by dejvino on 15.01.2017.
 */
public abstract class GameApplication extends NotSoSimpleApplication
{
    protected boolean gamePaused = false;
    protected BulletAppState bulletAppState;

    public GameApplication() {
    }

    public GameApplication(AppState... initialStates) {
        super(initialStates);
        attachDebugStates();
    }

    @Override
    public void initializeGame() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
    }

    protected PhysicsSpace getPhysicsSpace(){
        return bulletAppState.getPhysicsSpace();
    }

    public void setGamePaused(boolean paused)
    {
        boolean changed = (gamePaused != paused);
        if (changed) {
            gamePaused = paused;
            onGamePause(paused);
        }
    }

    protected void onGamePause(boolean paused)
    {
        bulletAppState.setEnabled(!paused);
    }
}
