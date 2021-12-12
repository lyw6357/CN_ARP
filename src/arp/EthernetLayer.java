package arp;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Arrays;

public class EthernetLayer implements BaseLayer {

    public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<>();
    private _ETHERNET_Frame ethernetHeader = new _ETHERNET_Frame();

    public EthernetLayer(String pName) {
        this.pLayerName = pName;
    }

    public void setDestNumber(byte[] array) {
        this.ethernetHeader.enet_dstaddr.setAddrData(array);
    }

    public void setSrcNumber(byte[] array) {
        this.ethernetHeader.enet_srcaddr.setAddrData(array);
    }

    public byte ethernetHeaderGetType(int index) {
        return this.ethernetHeader.enet_type[index];
    }

    private class _ETHERNET_ADDR {
        private byte[] addr = new byte[6];

        public _ETHERNET_ADDR() {
            for (int indexOfAddr = 0; indexOfAddr < addr.length; ++indexOfAddr) {
                this.addr[indexOfAddr] = (byte) 0x00;
            }
        }

        public byte getAddrData(int index) {
            return this.addr[index];
        }

        public void setAddrData(byte[] data) {
            this.addr = data;
        }
    }

    private class _ETHERNET_Frame {
        _ETHERNET_ADDR enet_dstaddr;
        _ETHERNET_ADDR enet_srcaddr;
        byte[] enet_type;
        byte[] enet_data;

        public _ETHERNET_Frame() {
            this.enet_dstaddr = new _ETHERNET_ADDR();
            this.enet_srcaddr = new _ETHERNET_ADDR();
            this.enet_type = new byte[2];
            this.enet_type[0] = 0x08;
            this.enet_type[1] = 0x08;
            this.enet_data = null;
        }
    }

    private byte[] etherNetDst() {
        return this.ethernetHeader.enet_dstaddr.addr;
    }

    private byte[] etherNetSrc() {
        return this.ethernetHeader.enet_srcaddr.addr;
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

    @Override
    public synchronized boolean Send(byte[] input, int length) {
        byte is_checked = input[0];
        byte[] headerAddedArray = new byte[length + 14];
        int index = 0;

        byte[] dst_ip = Arrays.copyOfRange(input, 25, 29);
        byte[] dst_mac = ARPLayer.getMacAddress(dst_ip);

        if (is_checked == 0x06 && input[8] == 0x01) {
            while (index < 6) {
                headerAddedArray[index] = (byte) 0xff;
                index += 1;
            }
            headerAddedArray[13] = (byte) 0x06;
        } else if (is_checked == 0x06 && input[8] == 0x02) {
            while (index < 6) {
                headerAddedArray[index] = dst_mac[index];
                index += 1;
            }
            headerAddedArray[13] = (byte) 0x06;
        } else if (is_checked == 0x08) {

            dst_ip = Arrays.copyOfRange(input, 17, 21);
            dst_mac = ARPLayer.getMacAddress(dst_ip);

            while (index < 6) {
                headerAddedArray[index] = dst_mac[index];
                index += 1;
            }
            headerAddedArray[13] = this.ethernetHeader.enet_type[1];
        }

        while (index < 12) { 
            headerAddedArray[index] = this.ethernetHeader.enet_srcaddr.getAddrData(index - 6);
            index += 1;
        }
        headerAddedArray[12] = this.ethernetHeader.enet_type[0];
        System.arraycopy(input, 0, headerAddedArray, 14, length);

        return this.GetUnderLayer().Send(headerAddedArray, headerAddedArray.length);
    }

    @Override
    public synchronized boolean Receive(byte[] input) {
        if (!this.isMyAddress(input) && (this.isBoardData(input) || this.isMyConnectionData(input))
                && input[12] == 0x08) {
            byte[] removedHeaderData = this.removeCappHeaderData(input);
            if (input[13] == 0x08) {
                System.out.println(input);
                return this.GetUpperLayer(0).Receive(removedHeaderData); 
            } else if (input[13] == 0x06) {
                return this.GetUpperLayer(1).Receive(removedHeaderData); 
            }
        }
        return false;
    }

    private byte[] removeCappHeaderData(byte[] input) {
        byte[] removeCappHeader = new byte[input.length - 14];
        for (int index = 0; index < removeCappHeader.length; index++) {
            removeCappHeader[index] = input[index + 14];
        }

        return removeCappHeader;
    }

    private boolean checkTheFrameData(byte[] myAddressData, byte[] inputFrameData, int inputDataStartIndex) {// add prarmeter »ç¿ë,
        for (int index = inputDataStartIndex; index < inputDataStartIndex + 6; index++) {
            if (inputFrameData[index] != myAddressData[index - inputDataStartIndex]) {
                return false;
            }
        }
        return true;
    }

    private boolean isBoardData(byte[] inputFrameData) {
        byte[] boardData = new byte[6];
        for (int index = 0; index < 6; index++) {
            boardData[index] = (byte) 0xFF;
        }
        return this.checkTheFrameData(boardData, inputFrameData, 0);
    }

    private boolean isMyConnectionData(byte[] inputFrameData) {
        byte[] srcAddr = ARPDialog.MyMacAddress;
        return this.checkTheFrameData(srcAddr, inputFrameData, 0);
    }

    private boolean isMyAddress(byte[] inputFrameData) {
        byte[] srcAddr = this.etherNetSrc();
        return this.checkTheFrameData(srcAddr, inputFrameData, 6);
    }
    
	private boolean isBroadcast(byte[] bytes) {
		for(int i = 0; i< 6; i++)
			if (bytes[i] != (byte) 0xff)
				return false;
		return (bytes[12] == (byte) 0xff && bytes[13] == (byte) 0xff);
	}
    
	public boolean fileSend(byte[] input, int length) {
		if (isBroadcast(ethernetHeader.enet_dstaddr.addr)) {
			ethernetHeader.enet_type = intToByte2(0xff);
		}
		else {
			ethernetHeader.enet_type[0] = (byte) 0x20;
			ethernetHeader.enet_type[1] = (byte) 0x90;
		}
		byte[] bytes = ObjToByte(ethernetHeader, input, length);
		this.GetUnderLayer().Send(bytes, length + 14);

		return true;
	}
	
    public byte[] ObjToByte(_ETHERNET_Frame Header, byte[] input, int length) {
		byte[] buf = new byte[length + 14];
		for(int i = 0; i < 6; i++) {
			buf[i] = Header.enet_dstaddr.addr[i];
			buf[i+6] = Header.enet_srcaddr.addr[i];
		}			
		buf[12] = Header.enet_type[0];
		buf[13] = Header.enet_type[1];
		for (int i = 0; i < length; i++)
			buf[14 + i] = input[i];

		return buf;
	}
	
    private byte[] intToByte2(int value) {
        byte[] temp = new byte[2];
        temp[0] |= (byte) ((value & 0xFF00) >> 8);
        temp[1] |= (byte) (value & 0xFF);

        return temp;
    }
}
