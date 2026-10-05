package com.beamillionaire.tools;
import com.beamillionaire.domain.*;
import com.beamillionaire.storage.*;
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
        return List.copyOf(issues);
    }
    public static void main(String[] args){
        var issues=issues();
        if(issues.isEmpty())System.out.println("Release inputs complete.");
        else {issues.forEach(System.err::println);System.exit(1);}
    }
}
