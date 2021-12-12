package arp;
import java.util.ArrayList;

public class TCPLayer implements BaseLayer {
    public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<>();
    private TCPHeader tcpHeader;


    public TCPLayer(String name) {
        this.tcpHeader = new TCPHeader();
        this.pLayerName = name;

    }

    @Override
    public boolean Send(byte[] input, int length) {
        int resultLength = input.length;

        byte[] tcpSegment = new byte[resultLength + 24];
        this.inputHeaderData(tcpSegment, input);

        if (length == -1) {
            return this.GetUnderLayer().Send(tcpSegment, -1);
        }

        return this.GetUnderLayer().Send(tcpSegment, tcpSegment.length);
    }

    private void inputHeaderData(byte[] tcpSegment, byte[] data) {
        byte[] srcPort = this.tcpHeader.shortToByteArray(this.tcpHeader.tcpSrcPort);
        byte[] dstPort = this.tcpHeader.shortToByteArray(this.tcpHeader.tcpDstPort);
        byte[] seqNumber = this.tcpHeader.intToByteArray(this.tcpHeader.tcpSeq);
        byte[] ackNumber = this.tcpHeader.intToByteArray(this.tcpHeader.tcpAck);
        byte[] tcpWindow = this.tcpHeader.shortToByteArray(this.tcpHeader.tcpWindow);
        byte[] tcpCksum = this.tcpHeader.shortToByteArray(this.tcpHeader.tcpCksum);
        byte[] tcpUrgptr = this.tcpHeader.shortToByteArray(this.tcpHeader.tcpUrgptr);

        tcpSegment[12] = this.tcpHeader.tcpOffset;
        tcpSegment[13] = this.tcpHeader.tcpFlag;

        for (int index = 0; index < 2; index++) {
            tcpSegment[index] = srcPort[index];
            tcpSegment[index + 2] = dstPort[index];
            tcpSegment[index + 14] = tcpWindow[index];
            tcpSegment[index + 16] = tcpCksum[index];
            tcpSegment[index + 18] = tcpUrgptr[index];
        }

        for (int index = 0; index < 4; index++) {
            tcpSegment[index + 4] = seqNumber[index];
            tcpSegment[index + 8] = ackNumber[index];
            tcpSegment[index + 20] = this.tcpHeader.padding[index];
        }

        for (int index = 0; index < data.length; index++) {
            tcpSegment[index + 24] = data[index];
        }
    }

    @Override
    public boolean Receive(byte[] input) {
        return this.GetUpperLayer(0).Receive(this.removeCapHeader(input));
    }

    private byte[] removeCapHeader(byte[] input) {
        byte[] removedArray = new byte[input.length - 24];
        System.arraycopy(input, 24, removedArray, 0, removedArray.length);

        return removedArray;
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
        this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);//layer�빊遺쏙옙
    }

    @Override
    public void SetUpperUnderLayer(BaseLayer pUULayer) {
        this.SetUpperLayer(pUULayer);
        pUULayer.SetUnderLayer(this);
    }


    private class TCPHeader {
        short tcpSrcPort;
        short tcpDstPort;
        int tcpSeq;
        int tcpAck;
        byte tcpOffset;
        byte tcpFlag;
        short tcpWindow;
        short tcpCksum;
        short tcpUrgptr;
        byte[] padding;
        byte[] tcpData;

        public TCPHeader() {
            this.padding = new byte[4];
        }

        byte[] shortToByteArray(short inputData) {
            byte[] arrayOfByte = new byte[2];
            arrayOfByte[0] = (byte) (inputData & 0xff);
            arrayOfByte[1] = (byte) ((inputData >> 8) & 0xff);

            return arrayOfByte;
        }

        byte[] intToByteArray(int inputData) {
            byte[] arrayOfByte = new byte[4];
            arrayOfByte[0] = (byte) (inputData & 0xff);
            arrayOfByte[1] = (byte) ((inputData >> 8) & 0xff);
            arrayOfByte[2] = (byte) ((inputData >> 16) & 0xff);
            arrayOfByte[3] = (byte) ((inputData >> 24) & 0xff);

            return arrayOfByte;
        }
    }
}