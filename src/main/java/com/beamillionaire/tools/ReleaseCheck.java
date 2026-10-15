package com.beamillionaire.tools;
import com.beamillionaire.domain.*;
import com.beamillionaire.storage.*;
import com.beamillionaire.ui.GameAudio;
import java.util.*;
import java.io.*;
import javax.sound.sampled.*;

/** Checks that the question bank and supplied audio are ready for release. */
public final class ReleaseCheck {
    public static List<String> issues() {
        var issues=new ArrayList<String>();
        try {
            var bank=new CsvQuestionRepository(AppPaths.questionDataDirectory()).load();
            for(var category:Category.values())for(var difficulty:Difficulty.values()) {
                int count=bank.pool(category,difficulty).size();
                if(count<QuestionBank.QUESTIONS_PER_DIFFICULTY)
                    issues.add(category+" / "+difficulty+": "+count+" questions; at least 5 required.");
            }
        }catch(IOException error){issues.add(error.getMessage());}
        issues.addAll(audioIssues());
        return List.copyOf(issues);
    }
    /** Resource decoding requires no speaker or sound card. */
    public static List<String> audioIssues(){
        var issues=new ArrayList<String>();
        for(String name:GameAudio.FILES){
            String path="/audio/"+name;
            try(var resource=ReleaseCheck.class.getResourceAsStream(path)){
                if(resource==null){issues.add("Missing audio: "+path);continue;}
                try(var stream=AudioSystem.getAudioInputStream(new BufferedInputStream(resource))){
                    var format=stream.getFormat();
                    if(!AudioFormat.Encoding.PCM_SIGNED.equals(format.getEncoding())||format.getSampleSizeInBits()!=16
                            ||format.isBigEndian()||format.getChannels()<1||format.getChannels()>2
                            ||format.getSampleRate()!=44100&&format.getSampleRate()!=48000
                            ||format.getFrameSize()!=format.getChannels()*2)
                        issues.add("Unsupported PCM WAV format: "+path);
                    byte[] buffer=new byte[8192];
                    long bytes=0;
                    for(int count;(count=stream.read(buffer))!=-1;)bytes+=count;
                    if(bytes==0)issues.add("Empty audio: "+path);
                    else if(stream.getFrameLength()!=AudioSystem.NOT_SPECIFIED
                            &&bytes!=stream.getFrameLength()*format.getFrameSize())
                        issues.add("Truncated audio: "+path);
                }
            }catch(IOException|UnsupportedAudioFileException error){issues.add("Unreadable audio: "+path+" ("+error.getMessage()+")");}
        }
        return List.copyOf(issues);
    }
    public static void main(String[] args){
        var issues=issues();
        if(issues.isEmpty())System.out.println("Release inputs complete.");
        else {issues.forEach(System.err::println);System.exit(1);}
    }
}
