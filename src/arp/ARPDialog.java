package arp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import static arp.Dialog.m_LayerMgr;

public class ARPDialog extends JFrame implements BaseLayer {
    public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<>();
    private BaseLayer fileUnderLayer;

    private JTextField ChattingWrite;

    private ArrayList<MacAndName> storageOfMacList = new ArrayList<>();

    private JTextArea IPAddressArea;
    private JPanel contentPane;
    private JButton ARPCacheSendButton;
    private static JTextArea ARPCacheTextArea;
    private JButton AllDeleteButton;
    private JButton ItemDeleteButton;
    private JButton AddButton;
    private JTextArea ProxyTextArea;
    private JButton DeleteButton;
    private JButton GratuitousARPSendButton;
    private JTextArea HWAddressArea;
    private JButton CancelButton;
    public static byte[] MyIPAddress;
    public static byte[] MyMacAddress;
    public static byte[] TargetIPAddress;

    public byte[] getMyIPAddress() {
        return MyIPAddress;
    }

    public void setMyIPAddress(byte[] myIPAddress) {
        MyIPAddress = myIPAddress;
    }

    public byte[] getMyMacAddress() {
        return MyMacAddress;
    }

    public void setMyMacAddress(byte[] myMacAddress) {
        MyMacAddress = myMacAddress;
    }

    public byte[] getTargetIPAddress() {
        return TargetIPAddress;
    }

    public void setTargetIPAddress(byte[] targetIPAddress) {
        TargetIPAddress = targetIPAddress;
    }
    
    public static byte[] GratuitousAddress;


    private static String MacToString(byte[] mac) {
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

    public static void updateARPTableToGUI() { // 최신화된 화면 보여줌
        //arp_table

        String result = "";
        for (String key : ARPLayer.arp_table.keySet()) {
            if (ARPLayer.arp_table.get(key).length != 1) {
                result += key + "          " + MacToString(ARPLayer.arp_table.get(key)) + "                    " + "Complete\n";
            } else {
            	result += key + "          " + "??????????" + "                    " + "Incomplete\n";
            }
        }
        ARPCacheTextArea.setText(result);

    }


    public ARPDialog(String pName) {
        this.pLayerName = pName;
    }

    public ARPDialog() {
        setTitle("TestARP");

        setBounds(100, 100, 867, 499);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JPanel ARPCachePanel = new JPanel();
        ARPCachePanel
                .setBorder(new TitledBorder(null, "ARP Cache", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        ARPCachePanel.setToolTipText("");
        ARPCachePanel.setBounds(14, 12, 384, 385);
        contentPane.add(ARPCachePanel);
        ARPCachePanel.setLayout(null);

        ARPCacheTextArea = new JTextArea();
        ARPCacheTextArea.setEditable(false);
        ARPCacheTextArea.setBounds(14, 23, 356, 189);
        ARPCachePanel.add(ARPCacheTextArea);

        ItemDeleteButton = new JButton("Item Delete");
        ItemDeleteButton.addActionListener(new setAddressListener());
        ItemDeleteButton.setBounds(24, 221, 135, 33);
        ARPCachePanel.add(ItemDeleteButton);

        AllDeleteButton = new JButton("All Delete");
        AllDeleteButton.addActionListener(new setAddressListener());
        AllDeleteButton.setBounds(209, 221, 135, 33);
        ARPCachePanel.add(AllDeleteButton);

        JLabel lblNewLabel = new JLabel("IP 주소");
        lblNewLabel.setBounds(14, 316, 62, 18);
        ARPCachePanel.add(lblNewLabel);

        IPAddressArea = new JTextArea();
        IPAddressArea.setBounds(78, 314, 200, 24);
        ARPCachePanel.add(IPAddressArea);

        ARPCacheSendButton = new JButton("Send");
        ARPCacheSendButton.addActionListener(new setAddressListener());
        ARPCacheSendButton.setBounds(293, 312, 77, 27);
        ARPCachePanel.add(ARPCacheSendButton);

        JPanel ProxyARPPanel = new JPanel();
        ProxyARPPanel.setBorder(
                new TitledBorder(null, "Proxy ARP Entry", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        ProxyARPPanel.setBounds(412, 12, 425, 260);
        contentPane.add(ProxyARPPanel);
        ProxyARPPanel.setLayout(null);

        ProxyTextArea = new JTextArea();
        ProxyTextArea.setEditable(false);
        ProxyTextArea.setBounds(14, 28, 397, 162);
        ProxyARPPanel.add(ProxyTextArea);

        AddButton = new JButton("Add");
        AddButton.addActionListener(new setAddressListener());
        AddButton.setBounds(48, 202, 135, 33);
        ProxyARPPanel.add(AddButton);

        DeleteButton = new JButton("Delete");
        DeleteButton.addActionListener(new setAddressListener());
        DeleteButton.setBounds(236, 202, 135, 33);
        ProxyARPPanel.add(DeleteButton);

        JPanel GratuitousARPPanel = new JPanel();
        GratuitousARPPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Gratuitous ARP",
                TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        GratuitousARPPanel.setBounds(412, 284, 425, 110);
        contentPane.add(GratuitousARPPanel);
        GratuitousARPPanel.setLayout(null);

        JLabel lblHw = new JLabel("H/W 주소");
        lblHw.setBounds(14, 47, 71, 18);
        GratuitousARPPanel.add(lblHw);

        HWAddressArea = new JTextArea();
        HWAddressArea.setBounds(99, 45, 202, 24);
        GratuitousARPPanel.add(HWAddressArea);

        GratuitousARPSendButton = new JButton("Send");
        GratuitousARPSendButton.addActionListener(new setAddressListener());
        GratuitousARPSendButton.setBounds(334, 43, 77, 27);
        GratuitousARPPanel.add(GratuitousARPSendButton);

        JButton QuitButton = new JButton("종료");
        QuitButton.addActionListener(e -> System.exit(0));
        QuitButton.setBounds(293, 409, 105, 27);
        contentPane.add(QuitButton);

        CancelButton = new JButton("취소");
        CancelButton.setBounds(410, 409, 105, 27);
        CancelButton.addActionListener(new setAddressListener());
        contentPane.add(CancelButton);
        setVisible(true);
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

    public byte[] getIPByteArray(String[] data) {
        byte[] newData = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            int temp = Integer.parseInt(data[i]);
            newData[i] = (byte) (temp & 0xFF);
        }
        return newData;
    }

    public byte[] getMacByteArray(String macAddress) {
        byte[] hexTobyteArrayMacAdress = new byte[6];
        String changeMacAddress = macAddress.replaceAll(":", "");
        for (int index = 0; index < 12; index += 2) {
            hexTobyteArrayMacAdress[index / 2] = (byte) ((Character.digit(changeMacAddress.charAt(index), 16) << 4)
                    + Character.digit(changeMacAddress.charAt(index + 1), 16));
        }
        return hexTobyteArrayMacAdress;
    }

    public class setAddressListener implements ActionListener {


        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == ARPCacheSendButton) { 
                String IPAddress = IPAddressArea.getText();
                byte[] IPAddressByteArray = getIPByteArray(IPAddress.split("\\."));
                setTargetIPAddress(IPAddressByteArray);
                Dialog.dstIPAddress.setText(IPAddress);

                if (!IPAddress.equals("") && (!ARPLayer.containMacAddress(IPAddressByteArray))) {
                	String newResultInArpCahceText = "IPAddress" + "          ??????????          Incomplete\n";
                    ARPCacheTextArea.append(newResultInArpCahceText);
                    IPAddressArea.setText("");
                    m_LayerMgr.GetLayer("TCP").Send(new byte[1], 1);
                } else if (ARPLayer.containMacAddress(IPAddressByteArray)
                        && ARPLayer.arp_table.get(ARPLayer.byteArrayToString(IPAddressByteArray)).length != 1) {
                    byte[] macAddress = ARPLayer.getMacAddress(IPAddressByteArray);
                    IPAddress = IPAddress + "          " + MacToString(macAddress) + "                    Complete\n";
                    ARPCacheTextArea.append(IPAddress);
                    IPAddressArea.setText("");
                }
            }
            if (e.getSource() == AllDeleteButton) { 
                if (ARPCacheTextArea.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "삭제할 ARP가 존재하지 않습니다.");
                    return;
                }
                int result = JOptionPane.showConfirmDialog(null, "모든 Cache를 삭제하시겠습니까?", "Cache Delete",
                        JOptionPane.OK_CANCEL_OPTION);
                if (result == 0) {
                    ARPCacheTextArea.setText("");
                    ARPLayer.RemoveAll_Arp();
                }
            }
            if (e.getSource() == ItemDeleteButton) { 
                if (ARPCacheTextArea.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "삭제할 ARP가 존재하지 않습니다.");
                    return;
                }
                String indexValue = JOptionPane.showInputDialog(null, "삭제할 Cache의 인덱스를 입력해주세요(Index : 1부터 시작)",
                        "Cache Delete", JOptionPane.OK_CANCEL_OPTION);
                int indexValueInteger = 0;
                if (indexValue != null) {
                    indexValueInteger = Integer.parseInt(indexValue);
                }
                String[] ARPCacheList = ARPCacheTextArea.getText().split("\n");
                String IPAddress = ARPCacheList[indexValueInteger - 1].split("          ")[0];
                String result = "";
                for (int i = 0; i < ARPCacheList.length; i++) {
                    if (i != indexValueInteger - 1) {
                        result = result + ARPCacheList[i] + "\n";
                    }
                }
                ARPCacheTextArea.setText(result);
                ARPLayer.Remove_Arp(getIPByteArray(IPAddress.split("\\.")));
            }
            if (e.getSource() == DeleteButton) { 
                if (ProxyTextArea.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "삭제할 Proxy가 존재하지 않습니다.");
                    return;
                }
                String indexValue = JOptionPane.showInputDialog(null, "삭제할 Proxy의 인덱스를 입력해주세요(Index : 1부터 시작)",
                        "Proxy Delete", JOptionPane.OK_CANCEL_OPTION);
                int indexValueInteger = 0;
                if (indexValue != null) {
                    indexValueInteger = Integer.parseInt(indexValue);
                }
                String[] ProxyData = ProxyTextArea.getText().split("\n");
                String result = "";
                for (int i = 0; i < ProxyData.length; i++) {
                    if (i != indexValueInteger - 1) {
                        result = result + ProxyData[i] + "\n";
                    } else {
                        String targetName = ProxyData[i].split("       ")[1];
                        ARPLayer.Remove_Proxy(getMacByteArray(targetName));
                    }
                }
                ProxyTextArea.setText(result);
            }
            if (e.getSource() == AddButton) {
                new ProxyDialog();
            }
            if (e.getSource() == GratuitousARPSendButton) {
                String HWAddress = HWAddressArea.getText();
                if (HWAddress.equals("")) {
                    JOptionPane.showMessageDialog(null, "정확한 주소를 입력해주세요.");
                } else {
                    GratuitousAddress = getMacByteArray(HWAddress);
                    MyMacAddress = GratuitousAddress;
                    Dialog.srcAddress.setText(HWAddress);

                    m_LayerMgr.GetLayer("TCP").Send(new byte[1], -1);
                    HWAddressArea.setText("");
                }
            }
            if (e.getSource() == CancelButton) {
                setVisible(false);
            }
        }
    }

    public class ProxyDialog extends JFrame {
        ProxyDialog() {
            setTitle("Proxy ARP Entry 추가");
            setBounds(100, 100, 450, 300);
            contentPane = new JPanel();
            contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
            setContentPane(contentPane);
            contentPane.setLayout(null);

            JLabel lblNewLabel = new JLabel("Device");
            lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            lblNewLabel.setBounds(28, 44, 86, 18);
            contentPane.add(lblNewLabel);

            JLabel lblIp = new JLabel("IP 주소");
            lblIp.setHorizontalAlignment(SwingConstants.RIGHT);
            lblIp.setBounds(28, 100, 86, 18);
            contentPane.add(lblIp);

            JLabel lblEthernet = new JLabel("   Ethernet 주소");
            lblEthernet.setBounds(28, 154, 86, 18);
            contentPane.add(lblEthernet);

            JTextArea DeviceText = new JTextArea();
            DeviceText.setBounds(128, 42, 255, 24);
            contentPane.add(DeviceText);

            JTextArea IPText = new JTextArea();
            IPText.setBounds(128, 98, 255, 24);
            contentPane.add(IPText);

            JTextArea EthernetText = new JTextArea();
            EthernetText.setBounds(128, 152, 255, 24);
            contentPane.add(EthernetText);

            JButton OkButton = new JButton("OK");
            OkButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String DeviceName = DeviceText.getText();
                    String IPName = IPText.getText();
                    String EthernetName = EthernetText.getText();
                    if (DeviceName.equals("") || IPName.equals("") || EthernetName.equals("")) {
                        JOptionPane.showMessageDialog(null, "올바른 정보를 입력해주세요");
                    } else {
                        ProxyTextArea.append(DeviceName + "       " + IPName + "       " + EthernetName + "\n");
                        byte[] IPArray = getIPByteArray(IPName.split("\\."));
                        byte[] EthernetArray = getMacByteArray(EthernetName);
                        DeviceText.setText("");
                        IPText.setText("");
                        EthernetText.setText("");
                        setVisible(false);
                        ARPLayer.Add_Proxy(IPArray, EthernetArray);
                    }
                }
            });
            OkButton.setBounds(63, 219, 105, 27);
            contentPane.add(OkButton);

            JButton CancelButton = new JButton("Cancel");
            CancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    DeviceText.setText("");
                    IPText.setText("");
                    EthernetText.setText("");
                    setVisible(false);
                }
            });
            CancelButton.setBounds(252, 219, 105, 27);
            contentPane.add(CancelButton);

            setVisible(true);
        }
    }
}