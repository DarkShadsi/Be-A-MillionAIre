package com.beamillionaire.ui.design;

import com.beamillionaire.domain.Choice;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import java.util.List;
import java.util.function.DoubleFunction;

/** Bounded type reduction, followed by scrolling, within a fixed design region. */
public final class QuestionContent extends ScrollPane {
    private final Label question=new Label();
    private final Label choices=new Label();
    private final DoubleFunction<Font> fonts;
    private final double maximum;
    private final double minimum;
    public QuestionContent(String text,List<Choice> options,DoubleFunction<Font> fonts,Color color,double maximum,double minimum){
        if(minimum<=0||maximum<minimum)throw new IllegalArgumentException("Invalid font bounds.");
        this.fonts=fonts;this.maximum=maximum;this.minimum=minimum;
        question.setText(text);question.setAlignment(Pos.CENTER);question.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        choices.setText(String.join("\n",options.stream().map(c->c.id()+". "+c.text()).toList()));
        choices.setAlignment(Pos.CENTER_LEFT);choices.setMaxWidth(440);
        for(Label label:List.of(question,choices)){
            label.setWrapText(true);label.setMinHeight(USE_PREF_SIZE);label.setTextFill(color);label.setLineSpacing(5);
            label.setStyle("-fx-text-fill: #" + color.toString().substring(2,8) + ";");
        }
        var content=new VBox(24,question,choices);content.setAlignment(Pos.TOP_CENTER);
        setContent(content);setFitToWidth(true);setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);setPannable(true);
        setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
        setId("questionContent");
    }
    @Override protected void layoutChildren(){
        double width=Math.max(1,getWidth()-24),size=maximum;
        var q=new Text(question.getText());q.setWrappingWidth(width);q.setLineSpacing(5);
        var a=new Text(choices.getText());a.setWrappingWidth(Math.min(440,width));a.setLineSpacing(5);
        for(;size>minimum;size-=1){
            q.setFont(fonts.apply(size));a.setFont(fonts.apply(size));
            if(q.getLayoutBounds().getHeight()+a.getLayoutBounds().getHeight()+24<=getHeight()-8)break;
        }
        Font font=fonts.apply(Math.max(minimum,size));question.setFont(font);choices.setFont(font);
        super.layoutChildren();
    }
    public double displayedFontSize(){return question.getFont().getSize();}
}
