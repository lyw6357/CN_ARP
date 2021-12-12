package arp;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class NILayer implements BaseLayer {
    public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<>();

    int m_iNumAdapter;
    public Pcap m_AdapterObject;
    public PcapIf device;
    public List<PcapIf> m_pAdapterList;
    StringBuilder errbuf = new StringBuilder();
    private Receive_Thread thread;
    private Receive_Thread fileThread;

    public void setThreadIsRun(boolean isRun) {
        this.thread.setIsRun(isRun);
    }

    public NILayer(String pName) {
        this.pLayerName = pName;
        m_pAdapterList = new ArrayList<>();
        m_iNumAdapter = 0;
        this.SetAdapterList();
    }

    public void SetAdapterList() {
        int r = Pcap.findAllDevs(m_pAdapterList, errbuf);
        if (r == Pcap.NOT_OK || m_pAdapterList.isEmpty()) {
            System.err.printf("Can't read list of devices, error is %s", errbuf.toString());
            return;
        }
    }

    public void setAdapterNumber(int iNum) {
        this.m_iNumAdapter = iNum;
        this.PacketStartDriver();
        this.Receive();
    }

    public void PacketStartDriver() {
        int snaplen = 64 * 1024;
        int flags = Pcap.MODE_PROMISCUOUS;
        int timeout = 10 * 1000;
        this.m_AdapterObject = Pcap.openLive(this.m_pAdapterList.get(this.m_iNumAdapter).getName(),
                snaplen, flags, timeout, this.errbuf);
    }

    @Override
    public String GetLayerName() {
        // TODO Auto-generated method stub
        return pLayerName;
    }

    @Override
    public BaseLayer GetUnderLayer() {
        // TODO Auto-generated method stub
        if (p_UnderLayer == null)
            return null;
        return p_UnderLayer;
    }

    @Override
    public BaseLayer GetUpperLayer(int nindex) {
        // TODO Auto-generated method stub
        if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
            return null;
        return p_aUpperLayer.get(nindex);
    }

    @Override
    public void SetUnderLayer(BaseLayer pUnderLayer) {
        // TODO Auto-generated method stub
        if (pUnderLayer == null)
            return;
        this.p_UnderLayer = pUnderLayer;
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

    @Override
    public boolean Receive() {
        thread = new Receive_Thread(this.m_AdapterObject, this.GetUpperLayer(0));
        Thread obj = new Thread(thread);
        obj.start();

        return false;
    }

    public boolean Send(byte[] input, int length) {
        ByteBuffer buf = ByteBuffer.wrap(input);
        if (m_AdapterObject.sendPacket(buf) != Pcap.OK) {
            System.err.println(m_AdapterObject.getErr());
            return false;
        }
        return true;
    }
    
    public PcapIf GetAdapterObject(int iIndex) {
		return m_pAdapterList.get(iIndex);
	}
}

class Receive_Thread implements Runnable {
    byte[] data;
    Pcap AdapterObject;
    BaseLayer UpperLayer;
    private boolean isRun = true;

    public void setIsRun(boolean isRun) {
        this.isRun = isRun;
    }

    public Receive_Thread(Pcap m_AdapterObject, BaseLayer m_UpperLayer) {
        this.AdapterObject = m_AdapterObject;
        this.UpperLayer = m_UpperLayer;
    }

    @Override
    public void run() {
        while (true) {
            if (!isRun) {
                System.out.println("Thread is terminated");
                return;
            }
            PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>() {
                public void nextPacket(PcapPacket packet, String user) {
                    data = packet.getByteArray(0, packet.size());
                    UpperLayer.Receive(data);
                }
            };
            AdapterObject.loop(10000, jpacketHandler, "");
        }
    }
}
