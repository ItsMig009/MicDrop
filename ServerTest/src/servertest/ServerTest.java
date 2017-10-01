
package servertest;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

class ServerTest {

AudioInputStream audioInputStream;
static AudioInputStream ais;
static AudioFormat format;
static boolean status = true;
static int port = 50005;
static int sampleRate = 8000; //44100
static Queue<byte[]> buffer = new ConcurrentLinkedQueue<byte[]>();

public static void main(String args[]) throws Exception {

   
    DatagramSocket serverSocket = new DatagramSocket(50005);

    byte[] receiveData = new byte[1280]; 
    // ( 1280 for 16 000Hz and 3584 for 44 100Hz (use AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) to get the correct size)

    format = new AudioFormat(sampleRate, 16, 1, true, false); 
        
//    new Thread(() -> {
//                      toSpeaker2();
//            }).start();
    
    while (status == true) {
        DatagramPacket receivePacket = new DatagramPacket(receiveData,
                receiveData.length);

        serverSocket.receive(receivePacket);
        
        buffer.add(receivePacket.getData());
        
        if(receivePacket.getData().length > 0){
            System.out.println("Incoming data...");
        }
        else{
            System.out.println("No data.");
        }

        ByteArrayInputStream baiss = new ByteArrayInputStream(
                receivePacket.getData());

        ais = new AudioInputStream(baiss, format, receivePacket.getLength());

        // A thread solve the problem of chunky audio 
        new Thread(new Runnable() {
            @Override
            public void run() {
                toSpeaker(receivePacket.getData());
            }
        }).start();
    }
     
}


public static void toSpeaker2() {
    //SourceDataLine speakers = null;
    try {

        while(true){
            while(!buffer.isEmpty()){
                byte[] soundbytes = buffer.poll();
                
                DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
                SourceDataLine speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                speakers.open(format);

                //FloatControl volumeControl = (FloatControl) speakers.getControl(FloatControl.Type.MASTER_GAIN);
                //volumeControl.setValue(5.0f);

                speakers.start();
                //speakers.open(format);
                //speakers.start();
                
                System.out.println("format? :" + speakers.getFormat());
                System.out.println("Lenght = " + soundbytes.length + " === " + soundbytes.toString());
                speakers.write(soundbytes, 0, soundbytes.length);
                speakers.drain();
                if(soundbytes.length > 0 && soundbytes[0] > 0){
                    System.out.println(soundbytes.toString());
                }
                speakers.close();
            }
            Thread.sleep(250);
        }
    } catch (Exception e) {
        System.out.println("Not working in speakers...");
        e.printStackTrace();
    }
}


//Original implementation

public static void toSpeaker(byte soundbytes[]) {
    try {

        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);

        sourceDataLine.open(format);

        FloatControl volumeControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
        volumeControl.setValue(5.0f);

        sourceDataLine.start();
        sourceDataLine.open(format);

        sourceDataLine.start();

        System.out.println("format? :" + sourceDataLine.getFormat());

        sourceDataLine.write(soundbytes, 0, soundbytes.length);
        System.out.println(soundbytes.toString());
        sourceDataLine.drain();
        sourceDataLine.close();
    } catch (Exception e) {
        System.out.println("Not working in speakers...");
        e.printStackTrace();
    }
}

}


