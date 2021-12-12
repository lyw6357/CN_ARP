package arp;
import java.util.ArrayList;

public class IPLayer implements BaseLayer {
    public int nUpperLayerCount = 0;
    public int nUnderLayerCount = 0;
    public String pLayerName = null;
    public ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<BaseLayer>();
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
    private _IP_Header ip_header = new _IP_Header();

    public IPLayer(String pName) {
        pLayerName = pName;
    }

    private byte[] RemoveCappHeader(byte[] input, int length) {

        byte[] temp = new byte[length - 21];
        System.arraycopy(input, 21, temp, 0, length - 21);
        return temp;
    }


    public boolean Send(byte[] input, int length) {
        int resultLength = input.length;
        this.ip_header.ip_dstaddr.addr = new byte[4]; 
        this.ip_header.ip_srcaddr.addr = new byte[4];
        SetIpSrcAddress(((ARPDialog) this.GetUpperLayer(0).GetUpperLayer(2)).getMyIPAddress());

        if (length == -1) { 
            SetIpDstAddress(((ARPDialog) this.GetUpperLayer(0).GetUpperLayer(2)).getMyIPAddress()); 
        } else {
            SetIpDstAddress(((ARPDialog) this.GetUpperLayer(0).GetUpperLayer(2)).getTargetIPAddress());
        }

        byte[] temp = ObjToByte21(this.ip_header, input, resultLength); 

        if (ARPLayer.containMacAddress(this.ip_header.ip_dstaddr.addr)) {
            return this.GetUnderLayer(0).Send(temp, resultLength + 21);
        }

        return this.GetUnderLayer(1).Send(temp, resultLength + 21);
    }

    private byte[] ObjToByte21(_IP_Header ip_header, byte[] input, int length) { 

        byte[] buf = new byte[length + 21];

        buf[0] = ip_header.is_checked;
        buf[1] = ip_header.ip_verlen;
        buf[2] = ip_header.ip_tos;
        buf[3] = (byte) (((length + 21) >> 8) & 0xFF);
        buf[4] = (byte) ((length + 21) & 0xFF);

        buf[5] = (byte) ((ip_header.ip_id >> 8) & 0xFF);
        buf[6] = (byte) (ip_header.ip_id & 0xFF);

        buf[7] = (byte) ((ip_header.ip_fragoff >> 8) & 0xFF);
        buf[8] = (byte) (ip_header.ip_fragoff & 0xFF);

        buf[9] = ip_header.ip_ttl;
        buf[10] = ip_header.ip_proto;

        buf[11] = (byte) ((ip_header.ip_cksum >> 8) & 0xFF);
        buf[12] = (byte) (ip_header.ip_cksum & 0xFF);


        System.arraycopy(ip_header.ip_srcaddr.addr, 0, buf, 13, 4);
        System.arraycopy(ip_header.ip_dstaddr.addr, 0, buf, 17, 4);
        System.arraycopy(input, 0, buf, 21, length);

        return buf;
    }

    public synchronized boolean Receive(byte[] input) {

        if (this.ip_header.ip_verlen != input[1] || this.ip_header.ip_tos != input[2]) {
            return false;
        } 

        int packet_tot_len = ((input[3] << 8) & 0xFF00) + input[4] & 0xFF; 

        for (int addr_index_count = 0; addr_index_count < 4; addr_index_count++) { 
            if (ARPDialog.MyIPAddress[addr_index_count] != input[17 + addr_index_count]) {  
                return this.GetUnderLayer(0).Send(input, packet_tot_len);  
            }
        }

        if (input[10] == 0x06) {
            return this.GetUpperLayer(0).Receive(RemoveCappHeader(input, packet_tot_len));
        }

        return false;
    }

    @Override
    public String GetLayerName() {
        return pLayerName;
    }


    public BaseLayer GetUnderLayer(int nindex) {
        if (nindex < 0 || nindex > nUnderLayerCount || nUnderLayerCount < 0)
            return null;
        return p_aUnderLayer.get(nindex);

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
        this.p_aUnderLayer.add(nUnderLayerCount++, pUnderLayer);
    }

    @Override
    public void SetUpperLayer(BaseLayer pUpperLayer) {
        // TODO Auto-generated method stub
        if (pUpperLayer == null)
            return;
        this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);

    }

    @Override
    public void SetUpperUnderLayer(BaseLayer pUULayer) {
        this.SetUpperLayer(pUULayer);
        pUULayer.SetUnderLayer(this);
    }

    public void SetIpSrcAddress(byte[] srcAddress) {
        ip_header.ip_srcaddr.addr = srcAddress;
    }
    
    public void SetIpDstAddress(byte[] dstAddress) {
        ip_header.ip_dstaddr.addr = dstAddress;

    }
    
    private class _IP_Header {
        byte is_checked; 
        byte ip_verlen; 
        byte ip_tos; 
        short ip_len;
        short ip_id; 
        short ip_fragoff;
        byte ip_ttl; 
        byte ip_proto;
        short ip_cksum; 

        _IP_ADDR ip_srcaddr;
        _IP_ADDR ip_dstaddr;

        private _IP_Header() {
            this.is_checked = 0x08;
            this.ip_verlen = 0x04; 
            this.ip_tos = 0x00;
            this.ip_len = 0;
            this.ip_id = 0;
            this.ip_fragoff = 0;
            this.ip_ttl = 0x00;
            this.ip_proto = 0x06;
            this.ip_cksum = 0;
            this.ip_srcaddr = new _IP_ADDR();
            this.ip_dstaddr = new _IP_ADDR();

        }

        private class _IP_ADDR {
            private byte[] addr = new byte[4];

            public _IP_ADDR() {
                this.addr[0] = 0x00;
                this.addr[1] = 0x00;
                this.addr[2] = 0x00;
                this.addr[3] = 0x00;
            }
        }
    }

    @Override
    public BaseLayer GetUnderLayer() {
        return null;
    }
}
