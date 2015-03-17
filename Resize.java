import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Component;

class Resize {
	public static void main(String[] args) throws InterruptedException {
		String filename = args[0];
		final SeamCarver sc = new SeamCarver(new Picture(filename));
		JFrame frame;
		frame = new JFrame();

		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menuBar.add(menu);
		JMenuItem menuItem1 = new JMenuItem("Save");
		menuItem1.addActionListener(sc);
		//menuItem1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
		 //                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menu.add(menuItem1);
		frame.setJMenuBar(menuBar);

		JFrame component = new JFrame("My Frame");

		frame.addComponentListener(new ComponentAdapter() 
		{  
		        public void componentResized(ComponentEvent evt) {
		            JFrame c = (JFrame)evt.getSource();
		            int w = c.getWidth();
		            int h = c.getHeight();
		            boolean update = false;
		            if (h < sc.height()) {
		            	int dif = sc.height() - h;
		            	//System.out.println("mudou h por " + dif);
		            	for (int i = 0; i < dif; i++) {
		            		sc.removeHorizontalSeam(sc.findHorizontalSeam());
		            		//System.out.println("i = " + i);
		            	}
		            	update = true;
		            	//System.out.println("update h");
		            }
		            if (w < sc.width()) {
		            	int dif = sc.width() - w;
		            	//System.out.println("mudou w por " + dif);
		            	for (int i = 0; i < dif; i++) {
		            		sc.removeVerticalSeam(sc.findVerticalSeam());
		            		//System.out.println("i = " + i);
		            	}
		            	update = true;
		            	//System.out.println("update w");
		            }
		            if (update) {
		         	c.setContentPane(sc.picture().getJLabel());
		         	c.repaint();
		         }
		        }
		});



		frame.setContentPane(sc.picture().getJLabel());
		// f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setTitle(filename);
		frame.setResizable(true);
		frame.pack();
		frame.setVisible(true);

		// draw
		frame.repaint();

		boolean mouseDown = false;

		//while (true) {
		    //if (StdDraw.mousePressed()) {
		    ///	mouseDown = true;
		    //}
		    //else if (mouseDown) {
		    mouseDown = false;
		    //int w = frame.getWidth();
		    //int h = frame.getHeight();
		    ////System.out.println("altura: " + h);
		    //boolean update = false;
		    //Thread.sleep(100);
		    //System.out.println("------------------------------------");
		    //System.out.println("h = " + h);
		    //System.out.println("w = " + w);
		    //System.out.println("------------------------------------");
//
		    //if (h < sc.height()) {
		    //	System.out.println("mudou h");
		    //	int dif = sc.height() - h;
		    //	for (int i = 0; i < dif; i++) {
		    //		sc.removeHorizontalSeam(sc.findHorizontalSeam());
		    //	}
		    //	update = true;
		    //	System.out.println("update h");
		    //}
		    //if (w < sc.width()) {
		    //	System.out.println("mudou w");
		    //	int dif = sc.width() - w;
		    //	for (int i = 0; i < dif; i++) {
		    //		sc.removeVerticalSeam(sc.findVerticalSeam());
		    //	}
		    //	update = true;
		    //	System.out.println("update w");
		    //}
		    //if (update) {
			//	frame.setContentPane(sc.picture().getJLabel());
			//	frame.repaint();
			//}

		    //}
		    //StdDraw.show(50);
		//}
	}
}