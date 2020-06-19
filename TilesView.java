package com.example.memo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;

class Card {
    int color, backColor = Color.DKGRAY;
    boolean isOpen = false; // цвет карты
    float x, y, width, height;

    public Card(float x, float y, float width, float height, int color) {
        this.color = color;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void draw(Canvas c) {
        Paint p = new Paint();
        // нарисовать карту в виде цветного прямоугольника
        if (isOpen) {
            p.setColor(color);
        } else p.setColor(backColor);
        c.drawRect(x,y, x+width, y+height, p);
    }
    public boolean flip (float touch_x, float touch_y) {
        if (touch_x >= x && touch_x <= x + width && touch_y >= y && touch_y <= y + height) {
            isOpen = ! isOpen;
            return true;
        } else return false;
    }

}

public class TilesView extends View {
    final int PAUSE_LENGTH = 1;
    boolean isOnPauseNow = false;

    // число открытых карт
    int openedCard = 0;
    int n = 6;

    ArrayList<Card> cards = new ArrayList<>();
    ArrayList<Integer> colors = new ArrayList<>();

    int width, height;
    int dx, dy;
    int current_x = 0, current_y = 0;

    MainActivity activity;

    boolean start = true;

    public TilesView(Context context) {
        super(context);
    }

    public TilesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        activity = (MainActivity) context;
        int color;
        Random r = new Random();

        for (int i = 0; i < n; i++) {
            color = Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255));
            for (int j = 0; j < 2; j++) {
                colors.add(color);
                Log.i("COLOR", "" + color);
            }
        }
    }

    public void addCard(int color) {
        if (current_x + dx >= width) {
            current_x = 0;
        }

        if (current_y + dy >= height) {
            current_y = 0;
        }

        if (current_x != 0) {
            current_x += 10;
        }

        if (current_y != 0) {
            current_y += 10;
        }

        cards.add(new Card (current_x, current_y, dx, dy, color));
        current_x += dx;
        current_y += dy;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (start) {
            width = canvas.getWidth();
            height = canvas.getHeight();
            dx = (width - 20) / 3;
            dy = (height - 30) / 4;

            while (colors.size() != 0) {
                int color = (int) (Math.random() * colors.size());
                addCard(colors.get(color));
                colors.remove(color);
            }

            start = false;
        }

        for (Card c : cards) {
            c.draw(canvas);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN && !isOnPauseNow)
        {
            for (Card c: cards) {

                if (openedCard == 0) {
                    if (c.flip(x, y)) {
                        Log.d("mytag", "card flipped: " + openedCard);
                        openedCard ++;
                        invalidate();
                        return true;
                    }
                }

                if (openedCard == 1) {
                    // перевернуть карту с задержкой
                    if (c.flip(x, y)) {
                        openedCard ++;
                        invalidate();


                        if (checkOpenCards(c)) {
                            if (cards.size() == 0) {
                                Toast toast = Toast.makeText(activity, "Game over", Toast.LENGTH_LONG);
                                toast.show();
                                return true;
                            }
                        }

                        PauseTask task = new PauseTask();
                        task.execute(PAUSE_LENGTH);
                        isOnPauseNow = true;

                        return true;
                    }


                }

            }
        }


        // заставляет экран перерисоваться
        return true;
    }

    public boolean checkOpenCards(Card card) {
        for (Card c: cards) {
            if (c.isOpen && ( card.x != c.x || card.y != c.y) && card.color == c.color) {
                cards.remove(c);
                cards.remove(card);
                return true;
            }
        }
        return false;
    }

    class PauseTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... integers) {
            Log.d("mytag", "Pause started");
            try {
                Thread.sleep(integers[0] * 1000);
            } catch (InterruptedException e) {}
            Log.d("mytag", "Pause finished");
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            for (Card c: cards) {
                if (c.isOpen) {
                    c.isOpen = false;
                }
            }
            openedCard = 0;
            isOnPauseNow = false;
            invalidate();
        }
    }
}
