package arp;
import org.jnetpcap.PcapIf;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Dialog extends JFrame implements BaseLayer {
    public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<>();
    private BaseLayer fileUnderLayer;
    private JButton CacheTableButton;

    public static LayerManager m_LayerMgr = new LayerManager();

    private JTextField ChattingWrite;
    static JTextField FileNameArea;
    
    private ArrayList<MacAndName> storageOfMacList = new ArrayList<>();

    public static ARPDialog arpDlg;

    public void setFileUnderLayer(BaseLayer newUnserLayer) {
        this.fileUnderLayer = newUnserLayer;
    }

    public BaseLayer fileUnderLayer() {
        return this.fileUnderLayer;
    }
    
	public File getFile() {
		return this.file;
	}

    Container contentPane;

    JTextArea ChattingArea;
    public static JTextArea srcAddress;
    public static JTextArea dstIPAddress;
    JTextArea fileUrl;
    JTextArea srcIPAddress;

    JLabel lblsrc;
    JLabel lbldst;
    JLabel lblNICLabel;


    JButton Setting_Button;
    JButton Chat_send_Button;
    JButton choiceFileButton;
    JButton sendFileButton;

    JComboBox<String> NICComboBox;
    int adapterNumber = 0;
    JProgressBar progressBar;

    File file;
    String Text;
    String FileNameText;
    
    public Dialog(String pName) {
        pLayerName = pName;

        setTitle("CN01_6_ARP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(250, 250, 644, 470);
        contentPane = new JPanel();
        ((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JPanel chattingPanel = new JPanel();
        chattingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "chatting",
                TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        chattingPanel.setBounds(10, 5, 360, 276);
        contentPane.add(chattingPanel);
        chattingPanel.setLayout(null);

        JPanel chattingEditorPanel = new JPanel();
        chattingEditorPanel.setBounds(10, 15, 340, 210);
        chattingPanel.add(chattingEditorPanel);
        chattingEditorPanel.setLayout(null);

        ChattingArea = new JTextArea();
        ChattingArea.setEditable(false);
        ChattingArea.setBounds(0, 0, 340, 210);
        chattingEditorPanel.add(ChattingArea);

        JPanel chattingInputPanel = new JPanel();
        chattingInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        chattingInputPanel.setBounds(10, 230, 250, 20);
        chattingPanel.add(chattingInputPanel);
        chattingInputPanel.setLayout(null);

        ChattingWrite = new JTextField();
        ChattingWrite.setBounds(2, 2, 250, 20);
        chattingInputPanel.add(ChattingWrite);
        ChattingWrite.setColumns(10);

        JPanel settingPanel = new JPanel();
        settingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "setting",
                TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        settingPanel.setBounds(380, 5, 236, 371);
        contentPane.add(settingPanel);
        settingPanel.setLayout(null);

        JPanel sourceAddressPanel = new JPanel();
        sourceAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        sourceAddressPanel.setBounds(10, 204, 170, 20);
        settingPanel.add(sourceAddressPanel);
        sourceAddressPanel.setLayout(null);

        srcAddress = new JTextArea();
        srcAddress.setBounds(0, 0, 170, 20);
        sourceAddressPanel.add(srcAddress);

        lblsrc = new JLabel("Source Mac Address");
        lblsrc.setBounds(10, 172, 170, 20);
        settingPanel.add(lblsrc);

        JPanel destinationAddressPanel = new JPanel();
        destinationAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        destinationAddressPanel.setBounds(10, 268, 170, 20);
        settingPanel.add(destinationAddressPanel);
        destinationAddressPanel.setLayout(null);

        dstIPAddress = new JTextArea();
        dstIPAddress.setBounds(0, 0, 170, 20);
        destinationAddressPanel.add(dstIPAddress);


        NILayer tempNI = (NILayer) m_LayerMgr.GetLayer("NI");
        if (tempNI != null) {
            for (int indexOfPcapList = 0; indexOfPcapList < tempNI.m_pAdapterList.size(); indexOfPcapList += 1) {
                final PcapIf inputPcapIf = tempNI.m_pAdapterList.get(indexOfPcapList);
                byte[] macAdress = null;
                try {
                    macAdress = inputPcapIf.getHardwareAddress();
                } catch (IOException e) {
                    System.out.println("Address error is happen");
                }
                if (macAdress == null) {
                    continue;
                }
                this.storageOfMacList.add(new MacAndName(macAdress, inputPcapIf.getDescription(), this.macByteToString(macAdress), indexOfPcapList));
            }
        }

        String[] nameOfConnection = new String[this.storageOfMacList.size()];
        for (int index = 0; index < this.storageOfMacList.size(); index++) {
            nameOfConnection[index] = this.storageOfMacList.get(index).macName;
        }

        this.NICComboBox = new JComboBox(nameOfConnection);
        this.NICComboBox.setBounds(10, 45, 170, 20);
        settingPanel.add(this.NICComboBox);
        this.NICComboBox.addActionListener(new setAddressListener());

        lblNICLabel = new JLabel("NIC ¼±ÅÃ");
        lblNICLabel.setBounds(10, 24, 170, 20);
        settingPanel.add(lblNICLabel);

        lbldst = new JLabel("Destination IP Address");
        lbldst.setBounds(10, 236, 190, 20);
        settingPanel.add(lbldst);

        Setting_Button = new JButton("Setting");
        Setting_Button.setBounds(80, 300, 100, 20);
        Setting_Button.addActionListener(new setAddressListener());
        settingPanel.add(Setting_Button);

        Chat_send_Button = new JButton("Send");
        Chat_send_Button.setBounds(270, 230, 80, 20);
        Chat_send_Button.addActionListener(new setAddressListener());
        Chat_send_Button.setEnabled(false);
        chattingPanel.add(Chat_send_Button);

        JPanel filePanel = new JPanel();

        filePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "File Transfer",
                TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        filePanel.setBounds(10, 285, 360, 90);
        contentPane.add(filePanel);
        filePanel.setLayout(null);

        JPanel fileEditorPanel = new JPanel();
        fileEditorPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        fileEditorPanel.setBounds(10, 20, 250, 20);
        filePanel.add(fileEditorPanel);
        fileEditorPanel.setLayout(null);

        fileUrl = new JTextArea();
        fileUrl.setEditable(false);
        fileUrl.setBounds(2, 2, 250, 20);
        fileEditorPanel.add(fileUrl);

        choiceFileButton = new JButton("File...");
        choiceFileButton.setBounds(270, 20, 80, 20);
        choiceFileButton.addActionListener(new setAddressListener());
        choiceFileButton.setEnabled(false);
        filePanel.add(choiceFileButton);

        this.progressBar = new JProgressBar(0, 100);
        this.progressBar.setBounds(10, 50, 250, 20);
        this.progressBar.setStringPainted(true);
        filePanel.add(this.progressBar);

        CacheTableButton = new JButton("CacheTable");
        CacheTableButton.addActionListener(new setAddressListener());
        CacheTableButton.setBounds(10, 332, 170, 27);
        settingPanel.add(CacheTableButton);

        JLabel lblSourceIpAddress = new JLabel("Source IP Address");
        lblSourceIpAddress.setBounds(10, 99, 170, 20);
        settingPanel.add(lblSourceIpAddress);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        panel.setBounds(10, 131, 170, 20);
        settingPanel.add(panel);

        srcIPAddress = new JTextArea();
        srcIPAddress.setBounds(0, 0, 170, 20);
        panel.add(srcIPAddress);

        sendFileButton = new JButton("transfer");
        sendFileButton.setBounds(270, 50, 80, 20);
        sendFileButton.addActionListener(new setAddressListener());
        sendFileButton.setEnabled(false);
        filePanel.add(sendFileButton);

        setVisible(true);
    }

    private String macByteToString(byte[] mac) {
        final StringBuilder sb = new StringBuilder();

        for (byte nowByte : mac) {
            if (sb.length() != 0) {
                sb.append(":");
            }
            if (0 <= nowByte && nowByte < 16) {
                sb.append("0");
            }
            sb.append(Integer.toHexString((nowByte < 0) ? nowByte + 256 : nowByte).toUpperCase());
        }
        return sb.toString();
    }

    public byte[] getIPByteArray(String[] data) {
        byte[] newData = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            int temp = Integer.parseInt(data[i]);
            newData[i] = (byte) (temp);
        }
        return newData;
    }

    private byte[] strToByte(String macAddress) {
        byte[] hexTobyteArrayMacAdress = new byte[6];
        String changeMacAddress = macAddress.replaceAll(":", "");
        for (int index = 0; index < 12; index += 2) {
            hexTobyteArrayMacAdress[index / 2] = (byte) ((Character.digit(changeMacAddress.charAt(index), 16) << 4)
                    + Character.digit(changeMacAddress.charAt(index + 1), 16));
        }
        return hexTobyteArrayMacAdress;
    }

    class setAddressListener implements ActionListener {

        private int findPortNumber(EthernetLayer tempEthernetLayer, String srcMacNumber) {
            for (int index = 0; index < storageOfMacList.size(); index++) {
                MacAndName tempMacObj = storageOfMacList.get(index);
                if (srcMacNumber.equals(tempMacObj.macAddressStr)) {
                    tempEthernetLayer.setSrcNumber(tempMacObj.macAddress);
                    return tempMacObj.portNumber;
                }
            }
            return 0;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ChatAppLayer tempChatAppLayer = (ChatAppLayer) m_LayerMgr.GetLayer("Chat");
            EthernetLayer tempEthernetLayer = (EthernetLayer) m_LayerMgr.GetLayer("Ethernet");
            NILayer tempNILayer = (NILayer) m_LayerMgr.GetLayer("NI");
            if (e.getSource() == Setting_Button) {
                if (e.getActionCommand().equals("Setting")) {
                    String srcMacNumber = srcAddress.getText();
                    adapterNumber = this.findPortNumber(tempEthernetLayer, srcMacNumber);
                    byte[] srcMacAddressArray = strToByte(srcMacNumber);
                    byte[] srcIPAddressArray = getIPByteArray(srcIPAddress.getText().split("\\."));
                    byte[] dstIPAddressArray = getIPByteArray((dstIPAddress.getText().split("\\.")));
                    ARPDialog.TargetIPAddress = dstIPAddressArray;
                    ARPDialog.MyIPAddress = srcIPAddressArray;
                    ARPDialog.MyMacAddress = srcMacAddressArray;
                    tempNILayer.setAdapterNumber(adapterNumber);
                    ((JButton) e.getSource()).setText("Reset");
                    this.enableAll(false);
                    this.enableForSendButtons(true);
                    sendFileButton.setEnabled(false);
                } else {
                    tempNILayer.setThreadIsRun(false);
                    byte[] resetByteArray = new byte[6];
                    tempEthernetLayer.setDestNumber(resetByteArray);
                    tempEthernetLayer.setSrcNumber(resetByteArray);
                    this.enableAll(true);
                    this.enableForSendButtons(false);
                    srcIPAddress.selectAll();
                    srcAddress.selectAll();
                    dstIPAddress.selectAll();
                    srcAddress.replaceSelection("");
                    dstIPAddress.replaceSelection("");
                    srcIPAddress.replaceSelection("");
                    ((JButton) e.getSource()).setText("Setting");
                }
            } else if (e.getSource() == Chat_send_Button) {
                String sendMessage = ChattingWrite.getText();
                if (sendMessage.equals("")) {
                    return;
                }
                byte[] arrayOfByte = sendMessage.getBytes();
                if (tempChatAppLayer.Send(arrayOfByte, arrayOfByte.length)) {
                    ChattingArea.append("[SEND] : " + sendMessage + "\n");
                } else {
                    ChattingArea.append("[Error] : send reject\n");
                }
                ChattingArea.selectAll();
                ChattingArea.setCaretPosition(ChattingArea.getDocument().getLength());
                ChattingWrite.selectAll();
                ChattingWrite.replaceSelection("");
            } else if (e.getSource() == NICComboBox) {
                int index = NICComboBox.getSelectedIndex();
                srcAddress.setText(storageOfMacList.get(index).macAddressStr);
            } else if (e.getSource() == choiceFileButton) {
				JFileChooser fileChoose = new JFileChooser();
				int value = fileChoose.showOpenDialog(null);
				if (value == JFileChooser.APPROVE_OPTION) {
					file = fileChoose.getSelectedFile();
					fileUrl.setText(file.getPath());
					sendFileButton.setEnabled(true);
					fileUrl.setEnabled(false);
					progressBar.setValue(0);
				}
            } else if (e.getSource() == CacheTableButton) {
                arpDlg.setVisible(true);
            } else if (e.getSource() == sendFileButton) {
            	((FileAppLayer)m_LayerMgr.GetLayer("FileApp")).setAndStartSendFile();
            }
        }

        private void enableAll(boolean enable) {
            srcAddress.setEnabled(enable);
            dstIPAddress.setEnabled(enable);
            NICComboBox.setEnabled(enable);
            srcIPAddress.setEnabled(enable);
        }

        private void enableForSendButtons(boolean enable) {
            Chat_send_Button.setEnabled(enable);
            choiceFileButton.setEnabled(enable);
            sendFileButton.setEnabled(enable);
        }
    }


    public boolean Receive(byte[] input) {
   
        String outputStr = new String(input);
        ChattingArea.append("[RECV] : " + outputStr + "\n");
        ChattingArea.selectAll();
        ChattingArea.setCaretPosition(ChattingArea.getDocument().getLength());
        System.out.println(outputStr);
        return true;
    }

    @Override
    public void SetUnderLayer(BaseLayer pUnderLayer) {
        if (pUnderLayer == null)
            return;
        this.p_UnderLayer = pUnderLayer;
    }

    @Override
    public void SetUpperLayer(BaseLayer pUpperLayer) {
        if (pUpperLayer == null)
            return;
        this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
    }

    @Override
    public String GetLayerName() {
        return pLayerName;
    }

    @Override
    public BaseLayer GetUnderLayer() {
        if (p_UnderLayer == null)
            return null;
        return p_UnderLayer;
    }

    @Override
    public BaseLayer GetUpperLayer(int nindex) {
        if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
            return null;
        return p_aUpperLayer.get(nindex);
    }

    @Override
    public void SetUpperUnderLayer(BaseLayer pUULayer) {
        this.SetUpperLayer(pUULayer);
        pUULayer.SetUnderLayer(this);
    }

    private class MacAndName {
        public byte[] macAddress;
        public String macName;
        public String macAddressStr;
        public int portNumber;

        public MacAndName(byte[] macAddress, String macName, String macAddressStr, int portNumberOfMac) {
            this.macAddress = macAddress;
            this.macName = macName;
            this.macAddressStr = macAddressStr;
            this.portNumber = portNumberOfMac;
        }
    }

    public static void main(String[] args) {

        m_LayerMgr.AddLayer(new NILayer("NI"));
        m_LayerMgr.AddLayer(new EthernetLayer("Ethernet"));
        m_LayerMgr.AddLayer(new ARPLayer("ARP"));
        m_LayerMgr.AddLayer(new IPLayer("IP"));
        m_LayerMgr.AddLayer(new TCPLayer("TCP"));
        m_LayerMgr.AddLayer(new ChatAppLayer("Chat"));
        m_LayerMgr.AddLayer(new FileAppLayer("File"));
        m_LayerMgr.AddLayer(new Dialog("FileGUI"));
        m_LayerMgr.AddLayer(new ARPDialog("ARPGUI"));
        m_LayerMgr.ConnectLayers(" NI ( *Ethernet ( *IP ( *TCP ( *Chat ( *FileGUI ) *File ( +FileGUI ) *ARPGUI )  -ARP ) *ARP ) )");
        arpDlg = new ARPDialog();
        arpDlg.setVisible(false);
        ((Dialog) m_LayerMgr.GetLayer("FileGUI")).setFileUnderLayer(m_LayerMgr.GetLayer("File"));
    }
}