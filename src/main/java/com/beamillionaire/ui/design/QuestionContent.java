package com.beamillionaire.ui.design;

import com.beamillionaire.domain.Choice;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleFunction;

/** Fits a stationary question and all four choices in the available reading area. */
public final class QuestionContent extends Pane {
    private static final double LETTER_GAP=10;
    private static final double MEASUREMENT_MARGIN=2;
    private static final double RIGHT_GUTTER=8;
    private final Label question=new Label();
    private final Rectangle separator=new Rectangle();
    private final List<Label> choiceLetters=new ArrayList<>();
    private final List<Label> choiceTexts=new ArrayList<>();
    private final DoubleFunction<Font> questionFonts;
    private final DoubleFunction<Font> choiceFonts;
    private final double maximum;
    private final double minimum;
    private final double maximumChoice;
    private final double minimumChoice;
    private FitKey fittedKey;
    private Measurement measurement;
    private double rowGap=18;
    private double headerGap=30;
    private boolean fits;

    public QuestionContent(String text,List<Choice> options,DoubleFunction<Font> fonts,Color color,double maximum,double minimum){
        this(text,options,fonts,fonts,color,maximum,minimum,30);
    }
    public QuestionContent(String text,List<Choice> options,DoubleFunction<Font> questionFonts,
                           DoubleFunction<Font> choiceFonts,Color color,double maximum,double minimum,double choiceSize){
        if(minimum<=0||maximum<minimum||choiceSize<=0)throw new IllegalArgumentException("Invalid font bounds.");
        this.questionFonts=Objects.requireNonNull(questionFonts);this.choiceFonts=Objects.requireNonNull(choiceFonts);
        this.maximum=maximum;this.minimum=minimum;this.maximumChoice=choiceSize;this.minimumChoice=Math.min(26,choiceSize);
        setId("questionContent");setMinSize(0,0);
        configure(question,"questionText");question.setLineSpacing(6);
        question.setFont(questionFonts.apply(maximum));
        separator.setManaged(false);separator.setMouseTransparent(true);separator.setHeight(1);
        getChildren().addAll(question,separator);
        for(int i=0;i<4;i++){
            var letter=new Label(String.valueOf((char)('A'+i))+".");
            configure(letter,"choiceLetter-"+(char)('A'+i));letter.setWrapText(false);
            letter.setFont(questionFonts.apply(choiceSize));
            var textLabel=new Label();configure(textLabel,"choiceText-"+(char)('A'+i));
            textLabel.setLineSpacing(8);textLabel.setFont(choiceFonts.apply(choiceSize));
            choiceLetters.add(letter);choiceTexts.add(textLabel);getChildren().addAll(letter,textLabel);
        }
        update("initial",text,options);setColors(color,Color.web("#008fbd"));
    }

    private static void configure(Label label,String id){
        label.setId(id);label.setManaged(false);label.setWrapText(true);
        label.setTextOverrun(OverrunStyle.CLIP);label.setAlignment(Pos.TOP_LEFT);
        label.setTextAlignment(TextAlignment.LEFT);label.setPadding(Insets.EMPTY);
        label.setMinSize(0,0);label.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
    }

    /** Text-only changes invalidate fitting; answer selection and help refreshes retain geometry. */
    public void update(String key,String text,List<Choice> options){
        Objects.requireNonNull(text);Objects.requireNonNull(options);
        var values=new ArrayList<String>();
        for(int i=0;i<4;i++){
            String id=String.valueOf((char)('A'+i));
            values.add(options.stream().filter(option->option.id().equals(id)).findFirst().orElseThrow().text());
        }
        boolean changed=!Objects.equals(question.getText(),text);
        for(int i=0;i<4;i++)changed|=!Objects.equals(choiceTexts.get(i).getText(),values.get(i));
        if(!changed)return;
        question.setText(text);question.setAccessibleText(text);
        for(int i=0;i<4;i++){
            choiceTexts.get(i).setText(values.get(i));
            choiceTexts.get(i).setAccessibleText((char)('A'+i)+". "+values.get(i));
        }
        fittedKey=null;requestLayout();
    }

    public void setColors(Color ink,Color accent){
        question.setTextFill(ink);separator.setFill(accent.deriveColor(0,1,1,.35));
        for(int i=0;i<4;i++){
            choiceTexts.get(i).setTextFill(ink);choiceLetters.get(i).setTextFill(accent);
        }
    }

    @Override protected void layoutChildren(){
        double width=Math.max(1,getWidth()),height=Math.max(1,getHeight());
        var key=new FitKey(question.getText(),choiceTexts.stream().map(Label::getText).toList(),width,height);
        if(!key.equals(fittedKey)){
            fit(width,height);fittedKey=key;
        }
        double textWidth=Math.max(1,width-measurement.letterWidth()-LETTER_GAP-RIGHT_GUTTER);
        question.resizeRelocate(0,0,width-RIGHT_GUTTER,measurement.questionHeight());
        separator.setX(0);separator.setY(measurement.questionHeight()+headerGap/2);
        separator.setWidth(width);
        double y=measurement.questionHeight()+headerGap;
        for(int i=0;i<4;i++){
            double rowHeight=measurement.choiceHeights()[i];
            choiceLetters.get(i).resizeRelocate(0,y,measurement.letterWidth(),rowHeight);
            choiceTexts.get(i).resizeRelocate(measurement.letterWidth()+LETTER_GAP,y,textWidth,rowHeight);
            y+=rowHeight+rowGap;
        }
        // Deliberately keep full measured heights if an imported question cannot fit.
        // The fit flag makes the issue detectable instead of silently hiding text.
        super.layoutChildren();
    }

    private void fit(double width,double height){
        question.applyCss();choiceLetters.forEach(Label::applyCss);choiceTexts.forEach(Label::applyCss);
        double questionSize=maximum,choiceSize=maximumChoice;
        setFonts(questionSize,choiceSize);setSpacing(6,8,18,30);
        measurement=measure(width);
        // Preserve type size before tightening line spacing and gaps between choices.
        if(measurement.height()>height){setSpacing(4,6,15,26);measurement=measure(width);}
        if(measurement.height()>height){setSpacing(2,4,12,22);measurement=measure(width);}
        while(measurement.height()>height&&(questionSize>minimum||choiceSize>minimumChoice)){
            questionSize=Math.max(minimum,questionSize-1);choiceSize=Math.max(minimumChoice,choiceSize-1);
            setFonts(questionSize,choiceSize);measurement=measure(width);
        }
        fits=measurement.height()<=height;
    }

    private void setFonts(double questionSize,double choiceSize){
        question.setFont(questionFonts.apply(questionSize));
        for(int i=0;i<4;i++){
            choiceLetters.get(i).setFont(questionFonts.apply(choiceSize));
            choiceTexts.get(i).setFont(choiceFonts.apply(choiceSize));
        }
    }
    private void setSpacing(double questionLine,double choiceLine,double rows,double header){
        question.setLineSpacing(questionLine);choiceTexts.forEach(label->label.setLineSpacing(choiceLine));
        rowGap=rows;headerGap=header;
    }
    private Measurement measure(double width){
        double letterWidth=42;
        for(var letter:choiceLetters)letterWidth=Math.max(letterWidth,Math.ceil(letter.prefWidth(-1)));
        double textWidth=Math.max(1,width-letterWidth-LETTER_GAP-RIGHT_GUTTER);
        double questionHeight=Math.ceil(question.prefHeight(Math.max(1,width-RIGHT_GUTTER)))+MEASUREMENT_MARGIN;
        double[] choiceHeights=new double[4];double height=questionHeight+headerGap+rowGap*3;
        for(int i=0;i<4;i++){
            choiceHeights[i]=Math.ceil(Math.max(choiceTexts.get(i).prefHeight(textWidth),
                    choiceLetters.get(i).prefHeight(letterWidth)))+MEASUREMENT_MARGIN;
            height+=choiceHeights[i];
        }
        return new Measurement(questionHeight,letterWidth,choiceHeights,height);
    }

    public Label questionLabel(){return question;}
    public List<Label> choiceLabels(){return List.copyOf(choiceTexts);}
    public List<Label> choiceLetterLabels(){return List.copyOf(choiceLetters);}
    public double displayedFontSize(){return question.getFont().getSize();}
    public double displayedChoiceFontSize(){return choiceTexts.get(0).getFont().getSize();}
    public boolean contentFits(){return fits;}
    public double totalMeasuredHeight(){return measurement==null?0:measurement.height();}
    private record FitKey(String question,List<String> choices,double width,double height){}
    private record Measurement(double questionHeight,double letterWidth,double[] choiceHeights,double height){}
}
