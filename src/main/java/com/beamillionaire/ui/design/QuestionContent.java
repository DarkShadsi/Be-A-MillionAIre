package com.beamillionaire.ui.design;

import com.beamillionaire.domain.Choice;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;

/** A stationary question above independently scrolling, generously spaced choices. */
public final class QuestionContent extends Pane {
    private final Label question=new Label();
    private final Hyperlink readFull=new Hyperlink("Read full question");
    private final ScrollPane choicesScroll=new ScrollPane();
    private final VBox choicesBox=new VBox(18);
    private final javafx.scene.shape.Rectangle separator=new javafx.scene.shape.Rectangle();
    private final List<Label> choiceLetters=new ArrayList<>();
    private final List<Label> choiceTexts=new ArrayList<>();
    private final DoubleFunction<Font> questionFonts;
    private final double maximum;
    private final double minimum;
    private Consumer<String> showFullQuestion=ignored->{};
    private String questionKey;
    private boolean overflow;

    public QuestionContent(String text,List<Choice> options,DoubleFunction<Font> fonts,Color color,double maximum,double minimum){
        this(text,options,fonts,fonts,color,maximum,minimum,30);
    }
    public QuestionContent(String text,List<Choice> options,DoubleFunction<Font> questionFonts,
                           DoubleFunction<Font> choiceFonts,Color color,double maximum,double minimum,double choiceSize){
        if(minimum<=0||maximum<minimum||choiceSize<=0)throw new IllegalArgumentException("Invalid font bounds.");
        this.questionFonts=questionFonts;this.maximum=maximum;this.minimum=minimum;
        setId("questionContent");
        question.setId("questionText");question.setWrapText(true);question.setLineSpacing(6);
        question.setAlignment(Pos.TOP_LEFT);question.setTextAlignment(javafx.scene.text.TextAlignment.LEFT);
        question.setPadding(Insets.EMPTY);question.setMinWidth(0);
        question.setManaged(false);
        readFull.setId("readFullQuestion");readFull.setManaged(false);readFull.setVisible(false);
        readFull.setPadding(Insets.EMPTY);readFull.setFont(choiceFonts.apply(24));
        readFull.setOnAction(event->showFullQuestion.accept(question.getText()));
        choicesBox.setFillWidth(true);choicesBox.setPadding(new Insets(2,8,8,0));
        for(int i=0;i<4;i++){
            var letter=new Label(String.valueOf((char)('A'+i))+".");letter.setMinWidth(42);letter.setPrefWidth(42);
            letter.setFont(questionFonts.apply(choiceSize));letter.setAlignment(Pos.TOP_LEFT);
            var textLabel=new Label();textLabel.setWrapText(true);textLabel.setLineSpacing(8);
            textLabel.setFont(choiceFonts.apply(choiceSize));textLabel.setMinWidth(0);textLabel.setMaxWidth(Double.MAX_VALUE);
            textLabel.setMinHeight(USE_PREF_SIZE);textLabel.setAlignment(Pos.TOP_LEFT);
            textLabel.setId("choiceText-"+(char)('A'+i));
            var row=new HBox(10,letter,textLabel);row.setAlignment(Pos.TOP_LEFT);row.setPadding(new Insets(6,10,6,8));
            row.setMinHeight(USE_PREF_SIZE);HBox.setHgrow(textLabel,Priority.ALWAYS);
            choiceLetters.add(letter);choiceTexts.add(textLabel);choicesBox.getChildren().add(row);
        }
        choicesScroll.setId("choicesScroll");choicesScroll.setManaged(false);choicesScroll.setContent(choicesBox);
        choicesScroll.setFitToWidth(true);choicesScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        choicesScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);choicesScroll.setPannable(true);
        choicesScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
        separator.setManaged(false);separator.setMouseTransparent(true);
        getChildren().addAll(question,readFull,separator,choicesScroll);
        update("initial",text,options);setColors(color,Color.web("#008fbd"));
    }

    /** Ordinary selection/help updates leave the user's reading position untouched. */
    public void update(String key,String text,List<Choice> options){
        boolean changed=!Objects.equals(questionKey,key);
        questionKey=key;question.setText(Objects.requireNonNull(text));question.setAccessibleText(text);
        for(int i=0;i<4;i++){
            String id=String.valueOf((char)('A'+i));
            String value=options.stream().filter(option->option.id().equals(id)).findFirst().orElseThrow().text();
            choiceTexts.get(i).setText(value);choiceTexts.get(i).setAccessibleText(id+". "+value);
        }
        if(changed)choicesScroll.setVvalue(0);
        requestLayout();
    }
    public void setOnReadFullQuestion(Consumer<String> action){showFullQuestion=Objects.requireNonNull(action);}
    public void setColors(Color ink,Color accent){
        question.setTextFill(ink);readFull.setTextFill(accent);
        separator.setFill(accent.deriveColor(0,1,1,.35));
        readFull.setStyle("-fx-border-color: transparent; -fx-underline: true;");
        for(int i=0;i<choiceTexts.size();i++){
            choiceTexts.get(i).setTextFill(ink);choiceLetters.get(i).setTextFill(accent);
        }
    }
    @Override protected void layoutChildren(){
        double width=Math.max(1,getWidth()),height=Math.max(1,getHeight());
        double availableHeader=Math.min(240,Math.max(80,height-198));
        question.applyCss();
        double size=maximum;
        while(true){
            question.setFont(questionFonts.apply(size));
            if(Math.ceil(question.prefHeight(width))+8<=availableHeader||size<=minimum)break;
            size=Math.max(minimum,size-1);
        }
        double textHeight=Math.ceil(question.prefHeight(width))+8;
        overflow=textHeight>availableHeader;
        double header=Math.min(availableHeader,Math.max(62,textHeight));
        question.setTextOverrun(overflow?javafx.scene.control.OverrunStyle.ELLIPSIS:javafx.scene.control.OverrunStyle.CLIP);
        question.resizeRelocate(0,0,width,overflow?header-34:header);
        readFull.setVisible(overflow);readFull.resizeRelocate(0,header-30,width,28);
        separator.setX(0);separator.setY(header+7);separator.setWidth(width);separator.setHeight(1);
        choicesScroll.resizeRelocate(0,header+18,width,Math.max(1,height-header-18));
        super.layoutChildren();
    }
    public Label questionLabel(){return question;}
    public ScrollPane choicesScroll(){return choicesScroll;}
    public boolean hasQuestionOverflow(){return overflow;}
    public double displayedFontSize(){return question.getFont().getSize();}
    /** Compatibility accessors now refer specifically to the independently scrolling choices. */
    public Node getContent(){return choicesScroll.getContent();}
    public Bounds getViewportBounds(){return choicesScroll.getViewportBounds();}
}
