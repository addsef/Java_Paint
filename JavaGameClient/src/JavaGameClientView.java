
// JavaObjClientView.java ObjecStram 기반 Client
//실질적인 채팅 창
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.ImageObserver;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class JavaGameClientView extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtInput;
	private String UserName;
	private JButton btnSend;
	private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의
	private Socket socket; // 연결소켓
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;

	private ObjectInputStream ois;
	private ObjectOutputStream oos;

	private JLabel lblUserName;
	// private JTextArea textArea;
	private JTextPane textArea;

	private Frame frame;
	private FileDialog fd;
	private JButton imgBtn;

	JPanel panel;
	private JLabel lblMouseEvent;
	private Graphics gc;
	private int pen_size = 2; // minimum 2
	// 그려진 Image를 보관하는 용도, paint() 함수에서 이용한다.
	private Image panelImage = null; 
	private Graphics2D gc2 = null;
	private Image forShapeImage;
	private Graphics2D gc3 = null;
	private JLabel PenSize;

	private Point oldPoint;
	private Point pressedPoint;
	private Point pressedPointforLine;

	private String shape_type = "free";
	private Color pen_color = Color.BLACK ;// default pen color : Black

	private JButton btnRedChange;
	private JButton btnGreenChange;
	private JButton btnBlueChange;
	private JButton btnBlackChange;
	
	private ImageIcon free_drawing = new ImageIcon(JavaGameClientView.class.getResource("/btnimage/drawing.png"));
	private ImageIcon rect_drawing = new ImageIcon(JavaGameClientView.class.getResource("/btnimage/rect.png"));
	private ImageIcon fill_rect_drawing = new ImageIcon(JavaGameClientView.class.getResource("/btnimage/rect-fill.png"));
	private ImageIcon oval_drawing = new ImageIcon(JavaGameClientView.class.getResource("/btnimage/oval.png"));
	private ImageIcon fill_oval_drawing = new ImageIcon(JavaGameClientView.class.getResource("/btnimage/oval-fill.png"));
	private ImageIcon line_drawing = new ImageIcon(JavaGameClientView.class.getResource("/btnimage/line.png"));

	public JavaGameClientView(String username, String ip_addr, String port_no)  {
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 634);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 352, 410);
		contentPane.add(scrollPane);

		textArea = new JTextPane();
		textArea.setEditable(true);
		textArea.setFont(new Font("굴림체", Font.PLAIN, 14));
		scrollPane.setViewportView(textArea);

		txtInput = new JTextField();
		txtInput.setBounds(74, 425, 209, 40);
		contentPane.add(txtInput);
		txtInput.setColumns(10);

		btnSend = new JButton("Send");
		btnSend.setFont(new Font("굴림", Font.PLAIN, 14));
		btnSend.setBounds(295, 425, 70, 40);
		contentPane.add(btnSend);

		lblUserName = new JLabel("Name");
		lblUserName.setBorder(new LineBorder(new Color(0, 0, 0)));
		lblUserName.setBackground(Color.WHITE);
		lblUserName.setFont(new Font("굴림", Font.BOLD, 14));
		lblUserName.setHorizontalAlignment(SwingConstants.CENTER);
		lblUserName.setBounds(12, 475, 62, 40);
		contentPane.add(lblUserName);
		setVisible(true);

		AppendText("User " + username + " connecting " + ip_addr + " " + port_no);
		UserName = username;
		lblUserName.setText(username);

		imgBtn = new JButton("+");
		imgBtn.setFont(new Font("굴림", Font.PLAIN, 16));
		imgBtn.setBounds(10, 425, 50, 40);
		contentPane.add(imgBtn);

		JButton btnNewButton = new JButton("종 료");
		btnNewButton.setFont(new Font("굴림", Font.PLAIN, 14));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChatMsg msg = new ChatMsg(UserName, "400", "Bye");
				SendObject(msg);
				System.exit(0);
			}
		});
		btnNewButton.setBounds(295, 475, 70, 40);
		contentPane.add(btnNewButton);

		panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.setBackground(Color.WHITE);
		panel.setBounds(376, 10, 400, 471);
		contentPane.add(panel);
		gc = panel.getGraphics();
		
		// Image 영역 보관용. paint() 에서 이용한다.
		panelImage = createImage(panel.getWidth(), panel.getHeight());
		gc2 = (Graphics2D) panelImage.getGraphics();
		gc2.setColor(Color.WHITE);
		gc2.fillRect(0,0, panel.getWidth(),  panel.getHeight());

		forShapeImage = createImage(panel.getWidth(), panel.getHeight());
		gc3 = (Graphics2D) forShapeImage.getGraphics();
		gc3.setColor(Color.WHITE);
		gc3.fillRect(0,0, panel.getWidth(),  panel.getHeight());

		lblMouseEvent = new JLabel("<dynamic>");
		lblMouseEvent.setHorizontalAlignment(SwingConstants.CENTER);
		lblMouseEvent.setFont(new Font("굴림", Font.BOLD, 14));
		lblMouseEvent.setBorder(new LineBorder(new Color(0, 0, 0)));
		lblMouseEvent.setBackground(Color.WHITE);
		lblMouseEvent.setBounds(376, 488, 400, 40);
		contentPane.add(lblMouseEvent);
		
		btnRedChange = new JButton("RED"); // 펜 색깔이 RED로  RED = 255, 0, 0 => setColor(RED)
		btnRedChange.setBounds(376, 539, 70, 40);
		btnRedChange.setForeground(Color.RED);
		btnRedChange.setFont(new Font("굴림", Font.BOLD, 14));
		contentPane.add(btnRedChange);
		btnRedChange.addActionListener(new BtnColorChange(Color.RED, btnRedChange));
		
		btnGreenChange = new JButton("GREEN");  // 펜 색깔이 GREEEN GREEN =
		btnGreenChange.setBounds(464, 540, 70, 40);
		btnGreenChange.setForeground(Color.GREEN);
		btnGreenChange.setFont(new Font("굴림", Font.BOLD, 14));
		contentPane.add(btnGreenChange);
		btnGreenChange.addActionListener(new BtnColorChange(Color.GREEN, btnGreenChange));
		
		btnBlueChange = new JButton("BLUE"); // 펜 색깔이 BULE =
		btnBlueChange.setBounds(546, 539, 70, 40);
		btnBlueChange.setForeground(Color.BLUE);
		btnBlueChange.setFont(new Font("굴림", Font.BOLD, 14));
		contentPane.add(btnBlueChange);
		btnBlueChange.addActionListener(new BtnColorChange(Color.BLUE, btnBlueChange));
		
		btnBlackChange = new JButton("BLACK"); // 펜 색깔이 BLACK  default Color = black
		btnBlackChange.setBounds(628, 539, 70, 40);
		btnBlackChange.setForeground(Color.BLACK);
		btnBlackChange.setFont(new Font("굴림", Font.BOLD, 14));
		contentPane.add(btnBlackChange);
		btnBlackChange.addActionListener(new BtnColorChange(Color.BLACK, btnBlackChange));
		
		JButton btnFreeDrawing = new JButton(free_drawing); // Free Drawing
		btnFreeDrawing.setBounds(12, 540, 40, 40);
		btnFreeDrawing.setBorderPainted(false);
		btnFreeDrawing.setContentAreaFilled(false);
		btnFreeDrawing.setFocusPainted(true);
		contentPane.add(btnFreeDrawing);
		btnFreeDrawing.addActionListener(new BtnShapeChange(btnFreeDrawing, "free"));
		
		JButton btnRectDrawing = new JButton(rect_drawing); // Draw Rectangle
		btnRectDrawing.setBounds(70, 540, 40, 40);
		btnRectDrawing.setBorderPainted(false);
		btnRectDrawing.setContentAreaFilled(false);
		btnRectDrawing.setFocusPainted(true);
		contentPane.add(btnRectDrawing);
		btnRectDrawing.addActionListener(new BtnShapeChange(btnRectDrawing, "rect"));
		
		JButton btnFillRectDrawing = new JButton(fill_rect_drawing); // Draw Fill Rectangle
		btnFillRectDrawing.setBounds(130, 540, 40, 40);
		btnFillRectDrawing.setBorderPainted(false);
		btnFillRectDrawing.setContentAreaFilled(false);
		btnFillRectDrawing.setFocusPainted(true);
		contentPane.add(btnFillRectDrawing);
		btnFillRectDrawing.addActionListener(new BtnShapeChange(btnFillRectDrawing, "fillrect"));
		
		JButton btnOvalDrawing = new JButton(oval_drawing); // Draw Oval
		btnOvalDrawing.setBounds(190, 540, 40, 40);
		btnOvalDrawing.setBorderPainted(false);
		btnOvalDrawing.setFocusPainted(true);
		btnOvalDrawing.setContentAreaFilled(false);
		contentPane.add(btnOvalDrawing);
		btnOvalDrawing.addActionListener(new BtnShapeChange(btnOvalDrawing, "oval"));
		
		JButton btnFillOvalDrawing = new JButton(fill_oval_drawing); // Draw Fil Oval
		btnFillOvalDrawing.setBounds(250, 540, 40, 40);
		btnFillOvalDrawing.setBorderPainted(false);
		btnFillOvalDrawing.setContentAreaFilled(false);
		btnFillOvalDrawing.setFocusPainted(true);
		contentPane.add(btnFillOvalDrawing);
		btnFillOvalDrawing.addActionListener(new BtnShapeChange(btnFillOvalDrawing, "filloval"));
		
		JButton btnLineDrawing = new JButton(line_drawing); // Draw Line
		btnLineDrawing.setBounds(310, 540, 40, 40);
		btnLineDrawing.setBorderPainted(false);
		btnLineDrawing.setContentAreaFilled(false);
		btnLineDrawing.setFocusPainted(true);
		contentPane.add(btnLineDrawing);
		btnLineDrawing.addActionListener(new BtnShapeChange(btnLineDrawing, "line"));
		
		JButton btnAllClear = new JButton("CLEAR"); // panelimage 지우기
		btnAllClear.setBounds(213, 477, 70, 40);
		contentPane.add(btnAllClear);
		btnAllClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ChatMsg cm = new ChatMsg(UserName, "800", "clear");
				SendObject(cm);
			}
		});

		PenSize = new JLabel("PEN = 2"); // set pan size   --> JLabel로 해서 server로부터 pen size 받기 
		PenSize.setBounds(710, 539, 70, 40);
		contentPane.add(PenSize);


		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no));
//			is = socket.getInputStream();
//			dis = new DataInputStream(is);
//			os = socket.getOutputStream();
//			dos = new DataOutputStream(os);

			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());

			// SendMessage("/login " + UserName);
			ChatMsg obcm = new ChatMsg(UserName, "100", "Hello");
			SendObject(obcm);

			ListenNetwork net = new ListenNetwork();
			net.start();
			TextSendAction action = new TextSendAction();
			btnSend.addActionListener(action);
			txtInput.addActionListener(action);
			txtInput.requestFocus();
			ImageSendAction action2 = new ImageSendAction();
			imgBtn.addActionListener(action2);
			MyMouseEvent mouse = new MyMouseEvent();
			panel.addMouseMotionListener(mouse);
			panel.addMouseListener(mouse);
			MyMouseWheelEvent wheel = new MyMouseWheelEvent();
			panel.addMouseWheelListener(wheel);


		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AppendText("connect error");
		}

	}
	

	public void paint(Graphics g) {  // 
		super.paint(g);
		gc.drawImage(panelImage, 0, 0, this);
	}
	
	
	// color change listener 
	class BtnColorChange implements ActionListener {
		Color color;
		JButton btn;
		public BtnColorChange(Color c, JButton b) {
				color = c;
				btn = b;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			ChatMsg cm = new ChatMsg(UserName, "600", "color change"); // 600
			cm.pen_color = color;
			SendObject(cm); 
		}
	}
	
	// server로부터 설정된 pen color set    color = cm.pen_color
	public void revColor(Color color) {
		pen_color = color;
	}

	class BtnShapeChange implements ActionListener {
		JButton btn;
		String shape_type;
		public BtnShapeChange(JButton b, String shape) {
			btn = b;
			this.shape_type = shape;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			ChatMsg cm = new ChatMsg(UserName, "700", "Change Shape"); // 700
			cm.shape_type = this.shape_type;
			SendObject(cm);
		}
	}

	public void revShape(String shape) {
		shape_type = shape; // default shape type = free drawing
	}
	
	public void clearPanel() {
		gc2.clearRect(0,0,panel.getWidth(),panel.getHeight());
		gc2.setColor(Color.WHITE);
		gc2.fillRect(0,0, panel.getWidth(),  panel.getHeight());
		gc3.clearRect(0, 0, panel.getWidth(), panel.getHeight());
		gc3.setColor(Color.WHITE);
		gc3.drawRect(0,0, panel.getWidth(),  panel.getHeight());
		panel.repaint(); 
	}

	public void receiveDrawingEvent(ChatMsg cm) {
		pen_size = cm.pen_size;
		gc2.setColor(cm.pen_color);

		switch(cm.shape_type) {
			case "free":
				freeDrawing(cm);
				break;
			case "rect":
				shapeDrawing(cm);
				break;
			case "oval":
				shapeDrawing(cm);
				break;
			case "line":
				shapeDrawing(cm);
				break;
			case "fillrect":
				shapeDrawing(cm);
				break;
			case "filloval":
				shapeDrawing(cm);
				break;
		}
	}
	
	// Server Message를 수신해서 화면에 표시
	class ListenNetwork extends Thread {
		public void run() {
			while (true) {
				try {
					Object obcm = null;
					String msg = null;
					ChatMsg cm;
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
					if (obcm == null)
						break;
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						msg = String.format("[%s]\n%s", cm.UserName, cm.data);
					} else
						continue;
					switch (cm.code) {
					case "200": // chat message
						if (cm.UserName.equals(UserName))
							AppendTextR(msg); // 내 메세지는 우측에
						else
							AppendText(msg);
						break;
					case "300": // Image 첨부
						if (cm.UserName.equals(UserName))
							AppendTextR("[" + cm.UserName + "]");
						else
							AppendText("[" + cm.UserName + "]");
						AppendImage(cm.img);
					case "500": // Mouse Event 수신
						receiveDrawingEvent(cm);
						break;
					case "600": // change pen color
						 revColor(cm.pen_color);
						break;
						case "700":
							revShape(cm.shape_type);
							break;
						case "800":		
							clearPanel();
							break;

					}
				} catch (IOException e) {
					AppendText("ois.readObject() error");
					try {
//						dos.close();
//						dis.close();
						ois.close();
						oos.close();
						socket.close();

						break;
					} catch (Exception ee) {
						break;
					} // catch문 끝
				} // 바깥 catch문끝

			}
		}
	}

	public void freeDrawing(ChatMsg cm) {
		MouseEvent e = cm.mouse_e;
		if(cm.mouse_type.equals("pressed")) {
			oldPoint = e.getPoint();
		}
		gc2.setColor(pen_color);
		gc2.setStroke(new BasicStroke(cm.pen_size, BasicStroke.CAP_ROUND, 0));
		gc2.drawLine((int)oldPoint.getX(), (int)oldPoint.getY(), e.getX(), e.getY());
		gc.drawImage(panelImage, 0, 0, panel);
		oldPoint = e.getPoint();
		gc3.drawImage(panelImage, 0, 0, panel);
	}

	public void shapeDrawing(ChatMsg cm) {
		MouseEvent e = cm.mouse_e;

		switch(cm.mouse_type) {
			case "dragged":
				makeShape(pressedPoint, e.getPoint(), cm.shape_type);
				break;
			case "pressed":
				gc3.drawImage(forShapeImage, 0, 0, panel);
				pressedPoint = e.getPoint();
				pressedPointforLine = e.getPoint();
				break;
			case "released":
				makeShape(pressedPoint, e.getPoint(), cm.shape_type);
				gc3.drawImage(panelImage, 0, 0, panel);
				break;
			case "clicked":
				pressedPoint = e.getPoint();
		}
	}



	public void sendDrawingEvent(MouseEvent e, String mouse_type, String shape) {
		ChatMsg cm = new ChatMsg(UserName, "500", "DRAWING");
		cm.mouse_e = e; // 좌표
		cm.pen_size = pen_size;
		cm.mouse_type = mouse_type;
		cm.shape_type = shape;
		SendObject(cm);
	}

	class MyMouseWheelEvent implements MouseWheelListener {
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			// TODO Auto-generated method stub
			if (e.getWheelRotation() < 0) { // 위로 올리는 경우 pen_size 증가
				if (pen_size < 20)
					pen_size++;
			} else {
				if (pen_size > 2)
					pen_size--;
			}
			lblMouseEvent.setText("mouseWheelMoved Rotation=" + e.getWheelRotation()
					+ " pen_size = " + pen_size + " " + e.getX() + "," + e.getY());
			PenSize.setText("Pen : " + pen_size);
		}

	}
	class MyMouseEvent implements MouseListener, MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent e) {
			sendDrawingEvent(e, "dragged", shape_type);
		}
		@Override
		public void mouseClicked(MouseEvent e) {
			sendDrawingEvent(e, "clicked", shape_type);
		}
		@Override
		public void mousePressed(MouseEvent e) {
			sendDrawingEvent(e, "pressed", shape_type);
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			switch(shape_type) {
				case "rect":
					sendDrawingEvent(e, "released", "rect");
					break;
				case "oval":
					sendDrawingEvent(e, "released", "oval");
					break;
				case "line":
					sendDrawingEvent(e, "released", "line");
					break;
				case "fillrect":
					sendDrawingEvent(e, "released", "fillrect");
					break;
				case "filloval":
					sendDrawingEvent(e, "released", "filloval");
					break;
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}
		@Override
		public void mouseEntered(MouseEvent e) {
		}
		@Override
		public void mouseExited(MouseEvent e) {
		}
	}

	public void makeShape(Point pressedPoint, Point releasedPoint, String shape_type) {
		int x = (int) Math.min(pressedPoint.getX(), releasedPoint.getX());
		int y = (int) Math.min(pressedPoint.getY(), releasedPoint.getY());

		int width = (int) Math.abs(pressedPoint.getX() - releasedPoint.getX());
		int height = (int) Math.abs(pressedPoint.getY() - releasedPoint.getY());

		gc2.drawImage(forShapeImage, 0, 0, panel);
		gc2.setColor(pen_color);
		gc2.setStroke(new BasicStroke(pen_size, BasicStroke.CAP_ROUND, 0));

		switch(shape_type) {
			case "rect":
				gc2.drawRect(x, y, width, height);
				break;
			case "oval":
				gc2.drawOval(x, y, width, height);
				break;
			case "line":
				gc2.drawLine((int) pressedPointforLine.getX(), (int) pressedPointforLine.getY(),
						(int) releasedPoint.getX(), (int) releasedPoint.getY());
				break;
			case "filloval":
				gc2.fillOval(x, y, width, height);
				break;
			case "fillrect":
				gc2.fillRect(x, y, width, height);
				break;
		}
		gc.drawImage(panelImage, 0, 0, panel);
	}

	/*
	// Mouse Event 수신 처리
	public void DoMouseEvent(ChatMsg cm) {
		if (cm.UserName.matches(UserName)) // 본인 것은 이미 Local 로 그렸다.
			return;
		MouseEvent e = cm.mouse_e; // 마우스 좌표
		point = e.getPoint();
		gc2.setColor(pen_color);
		gc2.drawLine((int) point.getX(), (int) point.getY(), e.getX(), e.getY()); // point : double -> int
		gc.drawImage(panelImage, 0, 0, panel);
	}

	public void SendMouseEvent(MouseEvent e) {
		ChatMsg cm = new ChatMsg(UserName, "500", "MOUSE");
		cm.mouse_e = e;
		cm.pen_size = pen_size;
		SendObject(cm);
	}

	class MyMouseWheelEvent implements MouseWheelListener {
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			// TODO Auto-generated method stub
			if (e.getWheelRotation() < 0) { // 위로 올리는 경우 pen_size 증가
				if (pen_size < 20)
					pen_size++;
			} else {
				if (pen_size > 2)
					pen_size--;
			}
			lblMouseEvent.setText("mouseWheelMoved Rotation=" + e.getWheelRotation() 
				+ " pen_size = " + pen_size + " " + e.getX() + "," + e.getY());

		}
		
	}
	// Mouse Event Handler
	class MyMouseEvent implements MouseListener, MouseMotionListener {
		@Override
		public void mouseDragged(MouseEvent e) {
			lblMouseEvent.setText(e.getButton() + " mouseDragged " + e.getX() + "," + e.getY());// 좌표출력가능
			gc2.setColor(pen_color);
			gc2.fillOval(e.getX()-pen_size/2, e.getY()-pen_size/2, pen_size, pen_size);
			// panelImnage는 paint()에서 이용한다.
			gc.drawImage(panelImage, 0, 0, panel);
			SendMouseEvent(e);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			lblMouseEvent.setText(e.getButton() + " mouseMoved " + e.getX() + "," + e.getY());
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			lblMouseEvent.setText(e.getButton() + " mouseClicked " + e.getX() + "," + e.getY());
			gc2.setColor(pen_color);
			gc2.fillOval(e.getX()-pen_size/2, e.getY()-pen_size/2, pen_size, pen_size);
			gc.drawImage(panelImage, 0, 0, panel);
			SendMouseEvent(e);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			lblMouseEvent.setText(e.getButton() + " mouseEntered " + e.getX() + "," + e.getY());
			// panel.setBackground(Color.YELLOW);

		}

		@Override
		public void mouseExited(MouseEvent e) {
			lblMouseEvent.setText(e.getButton() + " mouseExited " + e.getX() + "," + e.getY());
			// panel.setBackground(Color.CYAN);

		}

		@Override
		public void mousePressed(MouseEvent e) {
			lblMouseEvent.setText(e.getButton() + " mousePressed " + e.getX() + "," + e.getY());

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			lblMouseEvent.setText(e.getButton() + " mouseReleased " + e.getX() + "," + e.getY());
			// 드래그중 멈출시 보임

		}
	}
	*/


	// keyboard enter key 치면 서버로 전송
	class TextSendAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Send button을 누르거나 메시지 입력하고 Enter key 치면
			if (e.getSource() == btnSend || e.getSource() == txtInput) {
				String msg = null;
				// msg = String.format("[%s] %s\n", UserName, txtInput.getText());
				msg = txtInput.getText();
				SendMessage(msg);
				txtInput.setText(""); // 메세지를 보내고 나면 메세지 쓰는창을 비운다.
				txtInput.requestFocus(); // 메세지를 보내고 커서를 다시 텍스트 필드로 위치시킨다
				if (msg.contains("/exit")) // 종료 처리
					System.exit(0);
			}
		}
	}

	class ImageSendAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// 액션 이벤트가 sendBtn일때 또는 textField 에세 Enter key 치면
			if (e.getSource() == imgBtn) {
				frame = new Frame("이미지첨부");
				fd = new FileDialog(frame, "이미지 선택", FileDialog.LOAD);
				// frame.setVisible(true);
				// fd.setDirectory(".\\");
				fd.setVisible(true);
				// System.out.println(fd.getDirectory() + fd.getFile());
				if (fd.getDirectory().length() > 0 && fd.getFile().length() > 0) {
					ChatMsg obcm = new ChatMsg(UserName, "300", "IMG");
					ImageIcon img = new ImageIcon(fd.getDirectory() + fd.getFile());
					obcm.img = img;
					SendObject(obcm);
				}
			}
		}
	}

	ImageIcon icon1 = new ImageIcon("src/icon1.jpg");

	public void AppendIcon(ImageIcon icon) {
		int len = textArea.getDocument().getLength();
		// 끝으로 이동
		textArea.setCaretPosition(len);
		textArea.insertIcon(icon);
	}

	// 화면에 출력
	public void AppendText(String msg) {
		// textArea.append(msg + "\n");
		// AppendIcon(icon1);
		msg = msg.trim(); // 앞뒤 blank와 \n을 제거한다.
		//textArea.setCaretPosition(len);
		//textArea.replaceSelection(msg + "\n");
		
		StyledDocument doc = textArea.getStyledDocument();
		SimpleAttributeSet left = new SimpleAttributeSet();
		StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);
		StyleConstants.setForeground(left, Color.BLACK);
	    doc.setParagraphAttributes(doc.getLength(), 1, left, false);
		try {
			doc.insertString(doc.getLength(), msg+"\n", left );
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int len = textArea.getDocument().getLength();
		textArea.setCaretPosition(len);
		//textArea.replaceSelection("\n");


	}
	// 화면 우측에 출력
	public void AppendTextR(String msg) {
		msg = msg.trim(); // 앞뒤 blank와 \n을 제거한다.	
		StyledDocument doc = textArea.getStyledDocument();
		SimpleAttributeSet right = new SimpleAttributeSet();
		StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);
		StyleConstants.setForeground(right, Color.BLUE);	
	    doc.setParagraphAttributes(doc.getLength(), 1, right, false);
		try {
			doc.insertString(doc.getLength(),msg+"\n", right );
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block 
			e.printStackTrace();
		}
		int len = textArea.getDocument().getLength();
		textArea.setCaretPosition(len);
		//textArea.replaceSelection("\n");

	}
	
	public void AppendImage(ImageIcon ori_icon) {
		int len = textArea.getDocument().getLength();
		textArea.setCaretPosition(len); // place caret at the end (with no selection)
		Image ori_img = ori_icon.getImage();
		Image new_img;
		ImageIcon new_icon;
		int width, height;
		double ratio;
		width = ori_icon.getIconWidth();
		height = ori_icon.getIconHeight();
		// Image가 너무 크면 최대 가로 또는 세로 200 기준으로 축소시킨다.
		if (width > 200 || height > 200) {
			if (width > height) { // 가로 사진
				ratio = (double) height / width;
				width = 200;
				height = (int) (width * ratio);
			} else { // 세로 사진
				ratio = (double) width / height;
				height = 200;
				width = (int) (height * ratio);
			}
			new_img = ori_img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			new_icon = new ImageIcon(new_img);
			textArea.insertIcon(new_icon);
		} else {
			textArea.insertIcon(ori_icon);
			new_img = ori_img;
		}
		len = textArea.getDocument().getLength();
		textArea.setCaretPosition(len);
		textArea.replaceSelection("\n");
		// ImageViewAction viewaction = new ImageViewAction();
		// new_icon.addActionListener(viewaction); // 내부클래스로 액션 리스너를 상속받은 클래스로
		// panelImage = ori_img.getScaledInstance(panel.getWidth(), panel.getHeight(), Image.SCALE_DEFAULT);
		
		
		gc2.drawImage(ori_img,  0,  0, panel.getWidth(), panel.getHeight(), panel);
		gc.drawImage(panelImage, 0, 0, panel.getWidth(), panel.getHeight(), panel);
	}

	// Windows 처럼 message 제외한 나머지 부분은 NULL 로 만들기 위한 함수
	public byte[] MakePacket(String msg) {
		byte[] packet = new byte[BUF_LEN];
		byte[] bb = null;
		int i;
		for (i = 0; i < BUF_LEN; i++)
			packet[i] = 0;
		try {
			bb = msg.getBytes("euc-kr");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		for (i = 0; i < bb.length; i++)
			packet[i] = bb[i];
		return packet;
	}

	// Server에게 network으로 전송
	public void SendMessage(String msg) {
		try {
			// dos.writeUTF(msg);
//			byte[] bb;
//			bb = MakePacket(msg);
//			dos.write(bb, 0, bb.length);
			ChatMsg obcm = new ChatMsg(UserName, "200", msg);
			oos.writeObject(obcm);
		} catch (IOException e) {
			// AppendText("dos.write() error");
			AppendText("oos.writeObject() error");
			try {
//				dos.close();
//				dis.close();
				ois.close();
				oos.close();
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.exit(0);
			}
		}
	}

	public void SendObject(Object ob) { // 서버로 메세지를 보내는 메소드
		try {
			oos.writeObject(ob);
		} catch (IOException e) {
			// textArea.append("메세지 송신 에러!!\n");
			AppendText("SendObject Error");
		}
	}
}
