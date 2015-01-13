package ru.uproom.gate.localinterface.domain;

/**
 * Created by osipenko on 11.01.15.
 */
public class ByteQueue {


    //##############################################################################################################
    //######    fields


    private byte q[];
    private int posPut, posGet;

    private ByteQueueNotify watcher;
    private int notifySize = 1;


    //##############################################################################################################
    //######    constructors / destructors


    public ByteQueue(int size, ByteQueueNotify watcher) {
        q = new byte[size + 1];
        posPut = posGet = 0;
        this.watcher = watcher;
    }


    public ByteQueue(ByteQueue ob) {
        posPut = ob.posPut;
        posGet = ob.posGet;
        this.watcher = ob.watcher;

        q = new byte[ob.q.length];

        for (int i = posGet + 1; i <= posPut; i++)
            q[i] = ob.q[i];
    }


    public ByteQueue(byte a[], ByteQueueNotify watcher) {
        posPut = 0;
        posGet = 0;
        q = new byte[a.length + 1];

        for (byte b : a) put(b);
        this.watcher = watcher;
    }


    //##############################################################################################################
    //######    getters / setters


    public void setNotifySize(int notifySize) {
        this.notifySize = notifySize;
        if (createNotify() && watcher != null)
            synchronized (watcher) {
                watcher.notify();
            }
    }


    //##############################################################################################################
    //######    methods


    //------------------------------------------------------------------------
    //  put new byte in queue

    private boolean createNotify() {
        int dataLength = 0;
        if (posGet < posPut) dataLength = posPut - posGet;
        else dataLength = (q.length - posGet) + posPut;
        return dataLength > notifySize;
    }


    //------------------------------------------------------------------------
    //  put new byte in queue

    public synchronized void put(byte b) {
        q[posPut] = b;
        posPut++;
        if (posPut >= q.length) posPut = 0;
        if (createNotify() && watcher != null) watcher.hasData(new byte[]{b});
    }


    //------------------------------------------------------------------------
    //  put new byte sequence in queue

    public synchronized void put(byte[] b) {

        int freeQ = 0;
        int part1 = 0;
        if (posGet <= posPut) {
            part1 = q.length - posPut;
            freeQ = part1 + posGet;
        }
        if (posGet > posPut) {
            part1 = posGet - posPut;
            freeQ = part1;
        }
        if (b.length > freeQ) return;

        if (b.length > part1) {
            System.arraycopy(b, 0, q, posPut, part1);
            posPut = b.length - part1;
            System.arraycopy(b, part1, q, 0, posPut);
        } else {
            System.arraycopy(b, 0, q, posPut, b.length);
            posPut += b.length;
            if (posPut >= q.length) posPut = 0;
        }

        if (createNotify() && watcher != null) watcher.hasData(b);
    }


    //------------------------------------------------------------------------
    //  get next byte from queue

    public synchronized byte get() {
        if (posGet == posPut) return (byte) 0;
        return q[posGet++];
    }


    //------------------------------------------------------------------------
    //  get next byte sequence from queue

    public synchronized byte[] get(int size) {
        byte[] bytes = null;
        if (posGet == posPut) return bytes;

        int inSize = size;
        if (posGet < posPut) {
            inSize = (posPut - posGet) > size ? size : posPut - posGet;
            bytes = new byte[inSize];
            System.arraycopy(q, posGet, bytes, 0, bytes.length);
            posGet += bytes.length;
        } else {
            inSize = (q.length - posGet + posPut) > size ? size : q.length - posGet + posPut;
            bytes = new byte[inSize];
            int part1 = (q.length - posGet) > size ? size : q.length - posGet;
            System.arraycopy(q, posGet, bytes, 0, part1);
            posGet += part1;
            if (part1 < size) {
                posGet += (inSize - part1);
                System.arraycopy(q, 0, bytes, part1, posGet);
            }
        }

        if (posGet >= q.length) posGet = 0;
        return bytes;
    }

}
