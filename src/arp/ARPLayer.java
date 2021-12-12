package arp;

import java.util.*;

public class ARPLayer implements BaseLayer {
    public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<>();
    public static Map<String, byte[]> arp_table = new Hashtable<>();
    public _ARP_HEADER arp_Header = new _ARP_HEADER();
    public static Map<String, byte[]> proxy_table = new Hashtable<>();

    private class _ARP_ADDR {
        private byte[] mac_addr = new byte[6];
        private byte[] ip_addr = new byte[4];

        public _ARP_ADDR() {
            for (int indexOfAddr = 0; indexOfAddr < mac_addr.length; ++indexOfAddr) {
                this.mac_addr[indexOfAddr] = (byte) 0x00;
            }
            for (int indexOfAddr = 0; indexOfAddr < ip_addr.length; ++indexOfAddr) {
                this.ip_addr[indexOfAddr] = (byte) 0x00;
            }
        }
    }

    private class _ARP_HEADER {
        byte is_checked; 
        byte[] arp_mac_type;
        byte[] arp_ip_type;
        byte arp_mac_addr_len;
        byte arp_ip_addr_len;
        byte[] arp_opcode;
        _ARP_ADDR arp_srcaddr;
        _ARP_ADDR arp_dstaddr;

        public _ARP_HEADER() {
            this.is_checked = 0x00;
            this.arp_mac_type = new byte[2];
            this.arp_ip_type = new byte[2];
            this.arp_mac_addr_len = 0x06;
            this.arp_ip_addr_len = 0x04;
            this.arp_opcode = new byte[2];
            this.arp_srcaddr = new _ARP_ADDR();
            this.arp_dstaddr = new _ARP_ADDR();
        }
    }

    public static boolean containMacAddress(byte[] input) {
        return arp_table.containsKey(byteArrayToString(input));
    }

    public static byte[] getMacAddress(byte[] input) {
        String ip = byteArrayToString(input);
        if (arp_table.containsKey(ip)) {
            return arp_table.get(ip);
        }
        return proxy_table.get(ip); 
    }

    @Override
    public synchronized boolean Send(byte[] input, int length) {
  
        this.arp_Header.arp_srcaddr.ip_addr = ARPDialog.MyIPAddress; 
        byte[] targetip = Arrays.copyOfRange(input, 13, 17);
        byte[] myip = Arrays.copyOfRange(input, 17, 21);
        if (Arrays.equals(targetip, myip)) { 
            this.arp_Header.arp_srcaddr.mac_addr = ARPDialog.GratuitousAddress; 
            this.arp_Header.arp_dstaddr.ip_addr = ARPDialog.MyIPAddress; 
            byte[] arp = ObjToByte_Send(arp_Header, (byte) 0x06, (byte) 0x01);

            return GetUnderLayer().Send(arp, arp.length);
        } else {
            this.arp_Header.arp_srcaddr.mac_addr = ARPDialog.MyMacAddress;
            this.arp_Header.arp_dstaddr.ip_addr = ARPDialog.TargetIPAddress;

            this.arp_Header.arp_dstaddr.mac_addr = new byte[6]; 

            byte[] headerAddedArray = ObjToByte_Send(arp_Header, (byte) 0x06, (byte) 0x01);
            arp_table.put(byteArrayToString(ARPDialog.TargetIPAddress), new byte[1]);
            ARPDialog.updateARPTableToGUI();

            return this.GetUnderLayer().Send(headerAddedArray, headerAddedArray.length);
        }
    }

    @Override
    public synchronized boolean Receive(byte[] input) {
        byte[] opcode = Arrays.copyOfRange(input, 7, 9);
        byte[] src_mac_address = Arrays.copyOfRange(input, 9, 15);
        byte[] src_ip_address = Arrays.copyOfRange(input, 15, 19);
        byte[] dst_ip_address = Arrays.copyOfRange(input, 25, 29);
        // 리시브드

        if (opcode[0] == 0x00 & opcode[1] == 0x01) {
            this.setTimer(src_ip_address, 180000);
            _ARP_HEADER response_header = new _ARP_HEADER();
            if (Arrays.equals(dst_ip_address, ARPDialog.MyIPAddress)) {
                response_header.arp_srcaddr.mac_addr = ARPDialog.MyMacAddress;
                response_header.arp_srcaddr.ip_addr = dst_ip_address;
                response_header.arp_dstaddr.mac_addr = src_mac_address;
                response_header.arp_dstaddr.ip_addr = src_ip_address;
                this.arpCheckAndPut(src_ip_address, src_mac_address);
            } else if (Arrays.equals(src_ip_address, dst_ip_address)) { 
                this.arpCheckAndPut(src_ip_address, src_mac_address);

                return true;
            } else {
                if (proxy_table.containsKey(byteArrayToString(dst_ip_address))) {
                    response_header.arp_srcaddr.mac_addr = ARPDialog.MyMacAddress;
                    response_header.arp_srcaddr.ip_addr = dst_ip_address;
                    response_header.arp_dstaddr.mac_addr = src_mac_address;
                    response_header.arp_dstaddr.ip_addr = src_ip_address;
                    arpCheckAndPut(src_ip_address, src_mac_address);
              
                } else {
                    this.arpCheckAndPut(src_ip_address, src_mac_address);

                    return false;
                }
            }
        
            byte[] response_arp = ObjToByte_Send(response_header, (byte) 0x06, (byte) 0x02); 

            return this.GetUnderLayer().Send(response_arp, response_arp.length);
        } else if (opcode[0] == 0x00 & opcode[1] == 0x02) {
            this.setTimer(src_ip_address, 1200000);
            arpCheckAndPut(src_ip_address, src_mac_address);

            return true;
        }

        return false;
    }

    private void setTimer(byte[] src_ip_address, long time) {
        Timer timer = new Timer(byteArrayToString(src_ip_address));
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                arp_table.remove(Thread.currentThread().getName());
                ARPDialog.updateARPTableToGUI();
            }
        };
        timer.schedule(task, time); 
    }

    public void arpCheckAndPut(byte[] src_ip_address, byte[] src_mac_address) {
        String stringIpAddress = byteArrayToString(src_ip_address);
        if (arp_table.containsKey(stringIpAddress)) {
            byte[] beforeMacAddress = arp_table.get(stringIpAddress);
            if (beforeMacAddress.length != 1) {
                Set<String> arpTableKeySet = arp_table.keySet();
                for (String arpTableKey : arpTableKeySet) {
                    if (Arrays.equals(arp_table.get(arpTableKey), beforeMacAddress)) {
                        arp_table.replace(arpTableKey, src_mac_address);
                    }
                }
            }
            arp_table.replace(byteArrayToString(src_ip_address), src_mac_address);
            ARPDialog.updateARPTableToGUI();
        } else {
            arp_table.put(stringIpAddress, src_mac_address);
            ARPDialog.updateARPTableToGUI();
        }
    }

    public static String byteArrayToString(byte[] addressByteArray) {
    	int[] newData = new int[addressByteArray.length];
        for (int i = 0; i < addressByteArray.length; i++) {
            newData[i] = (addressByteArray[i] & 0xFF);
        }
        StringBuilder stringBuilder = new StringBuilder();
        int lengthOfData = newData.length - 1;
        for (int index = 0; index < lengthOfData; index++) {
            stringBuilder.append(newData[index]).append(".");
        }

        stringBuilder.append(newData[lengthOfData]);

        return stringBuilder.toString();
    }

    public static byte[] StringToByte(String data) {
        String[] arrayOfString = data.split("\\.");

        byte[] resultAddress = new byte[arrayOfString.length];
        int length = resultAddress.length;

        for (int index = 0; index < length; index++) {
            resultAddress[index] = Byte.parseByte(arrayOfString[index]);
        }

        return resultAddress;
    }

    public byte[] ObjToByte_Send(_ARP_HEADER Header, byte is_checked, byte opcode) {
        byte[] buf = new byte[29]; 
        byte[] src_mac = Header.arp_srcaddr.mac_addr;
        byte[] src_ip = Header.arp_srcaddr.ip_addr;
        byte[] dst_mac = Header.arp_dstaddr.mac_addr;
        byte[] dst_ip = Header.arp_dstaddr.ip_addr;

        buf[0] = is_checked;
        buf[1] = 0x00;
        buf[2] = 0x01;
        buf[3] = 0x08;
        buf[4] = 0x00;
        buf[5] = Header.arp_mac_addr_len;
        buf[6] = Header.arp_ip_addr_len;
        buf[7] = 0x00;
        buf[8] = opcode;
        System.arraycopy(src_mac, 0, buf, 9, 6);
        System.arraycopy(src_ip, 0, buf, 15, 4);
        System.arraycopy(dst_mac, 0, buf, 19, 6);
        System.arraycopy(dst_ip, 0, buf, 25, 4);

        return buf;
    }

    public static void Add_Proxy(byte[] IP, byte[] Mac) {
        proxy_table.put(byteArrayToString(IP), Mac);
    }

    public static void Remove_Arp(byte[] removedIp) { 
        arp_table.remove(byteArrayToString(removedIp));
        ARPDialog.updateARPTableToGUI();
    }

    public static void RemoveAll_Arp() { 
        arp_table = new Hashtable<>();
        ARPDialog.updateARPTableToGUI();
    }

    public static void Remove_Proxy(byte[] removedIp) {
        proxy_table.remove(byteArrayToString(removedIp));
    }

    public ARPLayer(String name) {
        this.pLayerName = name;
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
    public void SetUpperUnderLayer(BaseLayer pUULayer) {
        this.SetUpperLayer(pUULayer);
        pUULayer.SetUnderLayer(this);
    }
}