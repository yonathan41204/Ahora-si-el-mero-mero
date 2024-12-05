package dev.undesarrolladormas.ensamblador.funcs;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JOptionPane;

public class musica {

   private static Clip clip;
    
    public static void reproducirAudio(String archivo){
        try{
            File musica = new File(archivo);
            
            AudioInputStream canalAudio = AudioSystem.getAudioInputStream(musica);
            
            clip =AudioSystem.getClip();
            
            clip.open(canalAudio);
            
            clip.start();
            
            
            
        }catch(Exception e){
            JOptionPane.showMessageDialog(null,"Error: "+e);
        }
    }
    
    public static void detenerMusica(){
        if(clip != null && clip.isRunning()){ //Si el clip NO es nulo Y se esta ejecutando
            clip.stop();
            clip.close();
        }
    }
    
    public static void loopMusica(String archivo){
        
         try {
            File musica = new File(archivo);
            
            AudioInputStream canalAudio = AudioSystem.getAudioInputStream(musica);
            
            clip =AudioSystem.getClip();
            
            clip.open(canalAudio);
            
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        

        } catch(Exception e){
            JOptionPane.showMessageDialog(null, "Error: " + e);
        }
            
    }
}
