package it.unibo.oop.reactivegui02;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.oop.JFrameUtil;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;

/*
     * Realizzare una classe ConcurrentGUI con costruttore privo di argomenti,
     * tale che quando istanziata crei un JFrame con l'aspetto mostrato nella
     * figura allegata (e contatore inizialmente posto a zero).
     *
     * Il contatore venga aggiornato incrementandolo ogni 100 millisecondi
     * circa, e il suo nuovo valore venga mostrato ogni volta (l'interfaccia sia
     * quindi reattiva).
     *
     * Alla pressione del pulsante "down", il conteggio venga da lì in poi
     * aggiornato decrementandolo; alla pressione del pulsante "up", il
     * conteggio venga da lì in poi aggiornato incrementandolo; e così via, in
     * modo alternato.
     *
     * Alla pressione del pulsante "stop", il conteggio si blocchi, e i tre
     * pulsanti vengano disabilitati. Per far partire l'applicazioni si tolga il
     * commento nel main qui sotto.
     *
     * Suggerimenti: - si mantenga la struttura dell'esercizio precedente - per
     * pilotare la direzione su/giù si aggiunga un flag booleano all'agente --
     * deve essere volatile? - la disabilitazione dei pulsanti sia realizzata
     * col metodo setEnabled
     */
/**
 * Second example of reactive GUI.
 */
public final class ConcurrentGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentGUI.class);
    private final JLabel display = new JLabel();

    /**
     * Builds a new CGUI.
     */
    public ConcurrentGUI() {
        super();
        JFrameUtil.dimensionJFrame(this);
        final JPanel panel = new JPanel();
        panel.add(display);
        final JButton up = new JButton("up");
        panel.add(up);
        final JButton down = new JButton("down");
        panel.add(down);
        final JButton stop = new JButton("stop");
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);
        /*
         * Create the counter agent and start it. This is actually not so good:
         * thread management should be left to
         * java.util.concurrent.ExecutorService
         */
        final Agent agent = new Agent();
        new Thread(agent).start();
        /*
         * Register a listener that stops it
         */
        up.addActionListener(e -> agent.increment());
        down.addActionListener(e -> agent.decrement());
        stop.addActionListener(e -> {
            agent.stopCounting();
            up.setEnabled(false);
            down.setEnabled(false);
        });
    }

    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private final class Agent implements Runnable {
        /*
         * Stop is volatile to ensure visibility. Look at:
         *
         * http://archive.is/9PU5N - Sections 17.3 and 17.4
         *
         * For more details on how to use volatile:
         *
         * http://archive.is/4lsKW
         *
         */
        private volatile boolean increment = true;
        private volatile boolean stop;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                if (this.increment){
                    try {
                        // The EDT doesn't access `counter` anymore, it doesn't need to be volatile
                        final var nextText = Integer.toString(this.counter);
                        SwingUtilities.invokeAndWait(() -> ConcurrentGUI.this.display.setText(nextText)); 
                        this.counter++;
                        Thread.sleep(100);
                    } catch (InvocationTargetException | InterruptedException ex) {
                        LOGGER.error(ex.getMessage(), ex);
                    }
                }
                else {
                    try {
                        // The EDT doesn't access `counter` anymore, it doesn't need to be volatile
                        final var nextText = Integer.toString(this.counter);
                        SwingUtilities.invokeAndWait(() -> ConcurrentGUI.this.display.setText(nextText)); 
                        this.counter--;
                        Thread.sleep(100);
                    } catch (InvocationTargetException | InterruptedException ex) {
                        LOGGER.error(ex.getMessage(), ex);
                    }
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }

        public void increment(){
            this.increment = true;
        }

        public void decrement(){
            this.increment = false;
        }
    }
}
