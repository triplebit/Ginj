package info.ginj.ui.listener;

import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * This Listener can be used when the UI must ignore "small" drag movements and consider them as clicks.
 * Usage: addMouseListener(new DragInsensitiveMouseClickListener(actualListener));
 * From https://stackoverflow.com/a/19003495/13551878
 */
public class DragInsensitiveMouseClickListener implements MouseInputListener {

    protected static final int MAX_CLICK_DISTANCE = 15;

    private final MouseInputListener target;

    public MouseEvent pressed;

    /**
     * Constructor
     * @param target the actual MouseInputListener to send "filtered" events to. e.g. a MouseInputAdapter
     */
    public DragInsensitiveMouseClickListener(MouseInputListener target) {
        this.target = target;
    }

    @Override
    public final void mousePressed(MouseEvent e) {
        pressed = e;
        target.mousePressed(e);
    }

    private int getDragDistance(MouseEvent e) {
        int distance = 0;
        distance += Math.abs(pressed.getXOnScreen() - e.getXOnScreen());
        distance += Math.abs(pressed.getYOnScreen() - e.getYOnScreen());
        return distance;
    }

    @Override
    public final void mouseReleased(MouseEvent e) {
        target.mouseReleased(e);

        if (pressed != null) {
            if (getDragDistance(e) < MAX_CLICK_DISTANCE) {
                MouseEvent clickEvent = new MouseEvent((Component) pressed.getSource(),
                        MouseEvent.MOUSE_CLICKED, e.getWhen(), pressed.getModifiersEx(),
                        pressed.getX(), pressed.getY(), pressed.getXOnScreen(), pressed.getYOnScreen(),
                        pressed.getClickCount(), pressed.isPopupTrigger(), pressed.getButton());
                target.mouseClicked(clickEvent);
            }
            pressed = null;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        //do nothing, handled by pressed/released handlers
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        target.mouseEntered(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        target.mouseExited(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (pressed != null) {
            if (getDragDistance(e) < MAX_CLICK_DISTANCE)
                return; //do not trigger drag yet (distance is in "click" perimeter
            pressed = null;
        }
        target.mouseDragged(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        target.mouseMoved(e);
    }
}