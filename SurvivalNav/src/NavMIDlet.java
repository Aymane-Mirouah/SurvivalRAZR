import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

public class NavMIDlet extends MIDlet {
    private Display display;
    private NavCanvas canvas;

    public void startApp() {
        display = Display.getDisplay(this);
        canvas = new NavCanvas(display, this);
        display.setCurrent(canvas);
    }
    public void pauseApp() {}
    public void destroyApp(boolean unconditional) {}
}

class NavCanvas extends Canvas implements CommandListener, Runnable {
    private Display display;
    private MIDlet midlet;

    private static final int COLOR_BG       = 0x0a1628;
    private static final int COLOR_HDR_BG   = 0x050d18;
    private static final int COLOR_HDR_TEXT = 0x4a9eff;
    private static final int COLOR_BODY     = 0xc8c8c8;
    private static final int COLOR_YELLOW   = 0xffcc00;
    private static final int COLOR_RED      = 0xff4444;
    private static final int COLOR_GREEN    = 0x44ff88;
    private static final int COLOR_SELECTED = 0x1a3a5c;
    private static final int COLOR_BORDER   = 0x2a5a8c;
    private static final int COLOR_WHITE    = 0xffffff;
    private static final int COLOR_ORANGE   = 0xff8800;

    private int screen       = 0;
    private int selectedItem = 0;
    private int menuScroll   = 0;
    private int step         = 0;
    private int pageScroll   = 0;

    // timer
    private boolean timerRunning = false;
    private boolean timerDone    = false;
    private int     timerSeconds = 0;
    private int     timerMax     = 900;
    private Thread  timerThread  = null;

    // sun clock
    private int     inputHour    = 12;
    private int     inputMinute  = 0;
    private boolean editingHour  = true;

    private Command exitCommand;

    private static final String[] MAIN_MENU = {
        "1. Shadow Compass",
        "2. Sun Clock",
        "3. Star Finder",
        "4. No Compass Guide",
        "5. About"
    };

    private static final String[] NO_COMPASS = {
        "METHODS SUMMARY:",
        "DAY - SHADOW METHOD:",
        "Stick + 15min wait",
        "= West to East line.",
        "First mark = West.",
        "",
        "DAY - SUN METHOD:",
        "Noon sun = South",
        "(Northern Hemisphere)",
        "Noon sun = North",
        "(Southern Hemisphere)",
        "",
        "DAY - WATCH METHOD:",
        "Point hour hand at sun.",
        "Midpoint between hand",
        "and 12 = South.",
        "",
        "NIGHT - STARS:",
        "Polaris = North",
        "Southern Cross = South",
        "",
        "TIP:Always use 2 methods",
        "to confirm direction.",
        "TIP:Practice in daylight",
        "before you need it.",
        "WARNING:Clouds block",
        "sun and star methods.",
        "WARNING:Metal objects",
        "affect improvised compass."
    };

    private static final String[] ABOUT_LINES = {
        "SURVIVAL NAV",
        "For Motorola RAZR V3",
        "",
        "A navigation assistant",
        "with no GPS needed.",
        "Uses ancient methods",
        "that always work.",
        "",
        "FEATURES:",
        "- Shadow compass timer",
        "- Sun clock calculator",
        "- Star finder diagrams",
        "- No compass guide",
        "",
        "BY: Aymane Mirouah",
        "Marrakech, Morocco",
        "2026",
        "",
        "github.com/",
        "Aymane-Mirouah/",
        "SurvivalRAZR",
        "",
        "LICENSE: GPLv3",
        "",
        "TIP:The best compass",
        "is knowledge."
    };

    public NavCanvas(Display display, MIDlet midlet) {
        this.display = display;
        this.midlet  = midlet;
        exitCommand  = new Command("Exit", Command.EXIT, 1);
        addCommand(exitCommand);
        setCommandListener(this);
    }

    protected void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        g.setColor(COLOR_BG);
        g.fillRect(0, 0, w, h);
        g.setColor(COLOR_BORDER);
        g.drawRect(1, 1, w-3, h-3);

        if      (screen == 0) drawMainMenu(g, w, h);
        else if (screen == 1) drawShadowCompass(g, w, h);
        else if (screen == 2) drawSunClock(g, w, h);
        else if (screen == 3) drawStarFinder(g, w, h);
        else if (screen == 4) drawScrollScreen(g, w, h, "NO COMPASS", NO_COMPASS);
        else if (screen == 5) drawScrollScreen(g, w, h, "ABOUT", ABOUT_LINES);
    }

    private Font bold()  { return Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD,  Font.SIZE_SMALL); }
    private Font plain() { return Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL); }
    private Font large() { return Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD,  Font.SIZE_LARGE); }

    private void drawHeader(Graphics g, String title, int w) {
        g.setColor(COLOR_HDR_BG);
        g.fillRect(2, 2, w-4, 22);
        g.setColor(COLOR_BORDER);
        g.drawLine(2, 24, w-3, 24);
        g.setColor(COLOR_HDR_TEXT);
        g.setFont(bold());
        g.drawString(title, w/2, 5, Graphics.TOP | Graphics.HCENTER);
    }

    private void drawFooter(Graphics g, int w, int h, String left, String right) {
        g.setColor(COLOR_HDR_BG);
        g.fillRect(2, h-20, w-4, 18);
        g.setColor(COLOR_BORDER);
        g.drawLine(2, h-20, w-3, h-20);
        g.setColor(COLOR_HDR_TEXT);
        g.setFont(plain());
        if (left  != null) g.drawString(left,  8,   h-18, Graphics.TOP | Graphics.LEFT);
        if (right != null) g.drawString(right, w-8, h-18, Graphics.TOP | Graphics.RIGHT);
    }

    private void drawMainMenu(Graphics g, int w, int h) {
        drawHeader(g, "SURVIVAL NAV", w);
        int itemH   = 18;
        int startY  = 28;
        int visible = (h - startY - 22) / itemH;
        for (int i = 0; i < visible && (i + menuScroll) < MAIN_MENU.length; i++) {
            int idx = i + menuScroll;
            int y   = startY + (i * itemH);
            if (idx == selectedItem) {
                g.setColor(COLOR_SELECTED);
                g.fillRect(4, y, w-8, itemH);
                g.setColor(COLOR_YELLOW);
                g.setFont(bold());
            } else {
                g.setColor(COLOR_BODY);
                g.setFont(plain());
            }
            g.drawString(MAIN_MENU[idx], 10, y+2, Graphics.TOP | Graphics.LEFT);
        }
        if (menuScroll > 0) {
            g.setColor(COLOR_HDR_TEXT); g.setFont(bold());
            g.drawString("^", w-10, startY, Graphics.TOP | Graphics.LEFT);
        }
        if (menuScroll + visible < MAIN_MENU.length) {
            g.setColor(COLOR_HDR_TEXT); g.setFont(bold());
            g.drawString("v", w-10, h-34, Graphics.TOP | Graphics.LEFT);
        }
        drawFooter(g, w, h, "CTR=SELECT", "0=EXIT");
    }

    private void drawScrollScreen(Graphics g, int w, int h, String title, String[] lines) {
        drawHeader(g, title, w);
        int lineH   = 16;
        int headerH = 26;
        int footerH = 22;
        int y       = headerH + 4;
        int visible = (h - headerH - footerH) / lineH;

        for (int i = pageScroll; i < lines.length; i++) {
            if (y + lineH > h - footerH) break;
            String line = lines[i];
            if (line.length() == 0) { y += lineH/2; continue; }
            if (line.startsWith("WARNING:")) {
                g.setColor(COLOR_RED); g.setFont(bold());
                g.drawString("!"+line.substring(8), 6, y, Graphics.TOP | Graphics.LEFT);
            } else if (line.startsWith("TIP:")) {
                g.setColor(COLOR_YELLOW); g.setFont(bold());
                g.drawString(">"+line.substring(4), 6, y, Graphics.TOP | Graphics.LEFT);
            } else if (line.endsWith(":")) {
                g.setColor(COLOR_YELLOW); g.setFont(bold());
                g.drawString(line, 6, y, Graphics.TOP | Graphics.LEFT);
            } else {
                g.setColor(COLOR_BODY); g.setFont(plain());
                g.drawString(line, 6, y, Graphics.TOP | Graphics.LEFT);
            }
            y += lineH;
        }
        if (pageScroll > 0) {
            g.setColor(COLOR_HDR_TEXT); g.setFont(bold());
            g.drawString("^", w-10, headerH+2, Graphics.TOP | Graphics.LEFT);
        }
        if (pageScroll + visible < lines.length) {
            g.setColor(COLOR_HDR_TEXT); g.setFont(bold());
            g.drawString("v", w-10, h-footerH-14, Graphics.TOP | Graphics.LEFT);
        }
        drawFooter(g, w, h, "UP/DN=SCROLL", "0=BACK");
    }

    private void drawShadowCompass(Graphics g, int w, int h) {
        drawHeader(g, "SHADOW COMPASS", w);

        if (step == 0) {
            g.setColor(COLOR_YELLOW); g.setFont(bold());
            g.drawString("STEP 1 of 4", w/2, 28, Graphics.TOP | Graphics.HCENTER);
            g.setColor(COLOR_BODY); g.setFont(plain());
            g.drawString("Find a flat sunny area.", w/2, 46, Graphics.TOP | Graphics.HCENTER);
            g.drawString("Push a straight stick", w/2, 62, Graphics.TOP | Graphics.HCENTER);
            g.drawString("into the ground.", w/2, 78, Graphics.TOP | Graphics.HCENTER);
            // stick diagram
            int cx = w/2;
            g.setColor(COLOR_GREEN);
            g.drawLine(cx, 100, cx, 135);
            g.fillArc(cx-3, 132, 6, 6, 0, 360);
            g.setColor(COLOR_YELLOW);
            g.drawLine(cx, 100, cx+30, 118);
            g.setColor(COLOR_HDR_TEXT); g.setFont(plain());
            g.drawString("shadow->", cx+2, 108, Graphics.TOP | Graphics.LEFT);
            drawFooter(g, w, h, "CTR=NEXT", "0=BACK");

        } else if (step == 1) {
            g.setColor(COLOR_YELLOW); g.setFont(bold());
            g.drawString("STEP 2 of 4", w/2, 28, Graphics.TOP | Graphics.HCENTER);
            g.setColor(COLOR_BODY); g.setFont(plain());
            g.drawString("Mark the shadow tip", w/2, 46, Graphics.TOP | Graphics.HCENTER);
            g.drawString("with a stone.", w/2, 62, Graphics.TOP | Graphics.HCENTER);
            g.setColor(COLOR_GREEN); g.setFont(bold());
            g.drawString("This mark = WEST", w/2, 80, Graphics.TOP | Graphics.HCENTER);
            // diagram
            int cx = w/2;
            g.setColor(COLOR_GREEN);
            g.drawLine(cx, 102, cx, 132);
            g.fillArc(cx-3, 129, 6, 6, 0, 360);
            g.setColor(COLOR_YELLOW);
            g.drawLine(cx, 102, cx-28, 120);
            g.setColor(COLOR_RED);
            g.fillArc(cx-31, 117, 7, 7, 0, 360);
            g.setColor(COLOR_HDR_TEXT); g.setFont(bold());
            g.drawString("W", cx-44, 117, Graphics.TOP | Graphics.LEFT);
            drawFooter(g, w, h, "CTR=START TIMER", "0=BACK");

        } else if (step == 2) {
            g.setColor(COLOR_YELLOW); g.setFont(bold());
            g.drawString("STEP 3 of 4", w/2, 28, Graphics.TOP | Graphics.HCENTER);
            g.setColor(COLOR_BODY); g.setFont(plain());
            g.drawString("Wait 15 minutes.", w/2, 46, Graphics.TOP | Graphics.HCENTER);

            int remaining = timerMax - timerSeconds;
            int mins = remaining / 60;
            int secs = remaining % 60;
            String timeStr = (mins < 10 ? "0":"") + mins + ":" + (secs < 10 ? "0":"") + secs;

            if (timerDone) {
                g.setColor(COLOR_GREEN); g.setFont(large());
                g.drawString("DONE!", w/2, 68, Graphics.TOP | Graphics.HCENTER);
                g.setColor(COLOR_YELLOW); g.setFont(bold());
                g.drawString("Now mark the new", w/2, 108, Graphics.TOP | Graphics.HCENTER);
                g.drawString("shadow tip!", w/2, 124, Graphics.TOP | Graphics.HCENTER);
                drawFooter(g, w, h, "CTR=NEXT", "0=BACK");
            } else if (timerRunning) {
                g.setColor(COLOR_GREEN); g.setFont(large());
                g.drawString(timeStr, w/2, 68, Graphics.TOP | Graphics.HCENTER);
                int barW = w-20;
                g.setColor(COLOR_BORDER);
                g.fillRect(10, 108, barW, 10);
                g.setColor(COLOR_GREEN);
                g.fillRect(10, 108, barW * timerSeconds / timerMax, 10);
                g.setColor(COLOR_BODY); g.setFont(plain());
                g.drawString("TIMING...", w/2, 124, Graphics.TOP | Graphics.HCENTER);
                drawFooter(g, w, h, "CTR=STOP", "0=BACK");
            } else {
                g.setColor(COLOR_ORANGE); g.setFont(large());
                g.drawString(timeStr, w/2, 68, Graphics.TOP | Graphics.HCENTER);
                g.setColor(COLOR_BODY); g.setFont(plain());
                g.drawString("Press CTR to start", w/2, 108, Graphics.TOP | Graphics.HCENTER);
                drawFooter(g, w, h, "CTR=START", "0=BACK");
            }

        } else if (step == 3) {
            g.setColor(COLOR_YELLOW); g.setFont(bold());
            g.drawString("STEP 4 of 4", w/2, 28, Graphics.TOP | Graphics.HCENTER);
            g.setColor(COLOR_BODY); g.setFont(plain());
            g.drawString("Mark new shadow tip.", w/2, 46, Graphics.TOP | Graphics.HCENTER);
            g.drawString("Draw line between", w/2, 60, Graphics.TOP | Graphics.HCENTER);
            g.drawString("the two marks.", w/2, 74, Graphics.TOP | Graphics.HCENTER);
            // compass rose
            int cx = w/2;
            int cy = 130;
            int r  = 38;
            g.setColor(COLOR_BORDER);
            g.drawArc(cx-r, cy-r, r*2, r*2, 0, 360);
            g.setColor(COLOR_HDR_TEXT);
            g.drawLine(cx-r, cy, cx+r, cy);
            g.setColor(COLOR_RED);
            g.fillArc(cx-r-4, cy-4, 8, 8, 0, 360);
            g.fillArc(cx+r-4, cy-4, 8, 8, 0, 360);
            g.setColor(COLOR_GREEN);
            g.drawLine(cx, cy, cx, cy-r);
            g.drawLine(cx, cy-r, cx-5, cy-r+8);
            g.drawLine(cx, cy-r, cx+5, cy-r+8);
            g.setColor(COLOR_HDR_TEXT); g.setFont(bold());
            g.drawString("W", cx-r-14, cy-7, Graphics.TOP | Graphics.LEFT);
            g.drawString("E", cx+r+4,  cy-7, Graphics.TOP | Graphics.LEFT);
            g.setColor(COLOR_GREEN);
            g.drawString("N", cx-4, cy-r-16, Graphics.TOP | Graphics.LEFT);
            g.setColor(COLOR_BODY); g.setFont(plain());
            g.drawString("1st mark = WEST", w/2, 172, Graphics.TOP | Graphics.HCENTER);
            drawFooter(g, w, h, "CTR=RESTART", "0=BACK");
        }
    }

    private void drawSunClock(Graphics g, int w, int h) {
        drawHeader(g, "SUN CLOCK", w);

        // instructions
        g.setColor(COLOR_BODY); g.setFont(plain());
        g.drawString("UP/DN=change  CTR=switch", w/2, 28, Graphics.TOP | Graphics.HCENTER);

        // time display
        String hourStr = (inputHour < 10 ? "0":"") + inputHour;
        String minStr  = (inputMinute < 10 ? "0":"") + inputMinute;

        g.setColor(editingHour ? COLOR_YELLOW : COLOR_BODY);
        g.setFont(large());
        g.drawString(hourStr, w/2-8, 44, Graphics.TOP | Graphics.RIGHT);
        g.setColor(COLOR_WHITE);
        g.drawString(":", w/2-6, 44, Graphics.TOP | Graphics.LEFT);
        g.setColor(!editingHour ? COLOR_YELLOW : COLOR_BODY);
        g.drawString(minStr, w/2+8, 44, Graphics.TOP | Graphics.LEFT);

        // editing indicator
        g.setColor(COLOR_BORDER);
        g.drawLine(4, 76, w-4, 76);

        // result block
        g.setColor(COLOR_HDR_TEXT); g.setFont(bold());
        g.drawString("SOUTH is:", w/2, 82, Graphics.TOP | Graphics.HCENTER);

        String direction = getSunDirection(inputHour, inputMinute);
        g.setColor(COLOR_GREEN); g.setFont(large());
        g.drawString(direction, w/2, 98, Graphics.TOP | Graphics.HCENTER);

        g.setColor(COLOR_BORDER);
        g.drawLine(4, 126, w-4, 126);

        String result = getSunResult(inputHour, inputMinute);
        g.setColor(COLOR_BODY); g.setFont(plain());
        g.drawString(result, w/2, 132, Graphics.TOP | Graphics.HCENTER);
        g.drawString("(N. Hemisphere)", w/2, 148, Graphics.TOP | Graphics.HCENTER);
        g.drawString("Noon sun = due South", w/2, 163, Graphics.TOP | Graphics.HCENTER);

        drawFooter(g, w, h, "CTR=SWITCH", "0=BACK");
    }

    private String getSunDirection(int hour, int minute) {
        float h = hour + minute / 60.0f;
        if (h > 12) h = 24 - h;
        float diff = 12 - h;
        if (diff < 0) diff = -diff;
        if (diff < 1)      return "AHEAD";
        else if (diff < 3) return "SLIGHT LEFT";
        else if (diff < 6) return "TO YOUR LEFT";
        else               return "BEHIND YOU";
    }

    private String getSunResult(int hour, int minute) {
        float h = hour + minute / 60.0f;
        if (h >= 11 && h <= 13) return "Sun is near south now";
        else if (h < 12)        return "Sun moving toward south";
        else                    return "Sun moving away from south";
    }

    private void drawStarFinder(Graphics g, int w, int h) {
        drawHeader(g, "STAR FINDER", w);

        String[] lines = {
            "FIND NORTH - POLARIS:",
            "Northern Hemisphere only.",
            "",
            "1. Find the Big Dipper.",
            "   Looks like a ladle",
            "   or cooking pot.",
            "",
            "2. Find the 2 stars on",
            "   the outer edge of",
            "   the cup.",
            "   These are pointer stars.",
            "",
            "3. Draw imaginary line",
            "   through them upward.",
            "   Extend it x5 length.",
            "",
            "4. The bright star there",
            "   is POLARIS = NORTH.",
            "",
            "TIP:Polaris never moves.",
            "All other stars rotate",
            "around it through the night.",
            "",
            "FIND SOUTH - S. CROSS:",
            "Southern Hemisphere only.",
            "",
            "1. Find the Southern Cross.",
            "   4 stars in a cross shape.",
            "   Long axis points South.",
            "",
            "2. Extend the long axis",
            "   downward x4.5 its length.",
            "",
            "3. That point in the sky",
            "   is due SOUTH.",
            "",
            "TIP:Southern Cross is",
            "visible all year in the",
            "Southern Hemisphere.",
            "",
            "WARNING:Stars near the",
            "horizon are unreliable.",
            "Use stars high in the sky.",
            "",
            "TIP:If unsure which star",
            "is Polaris - it is the",
            "one that does NOT move",
            "as the night progresses."
        };

        int lineH   = 16;
        int headerH = 26;
        int footerH = 22;
        int y       = headerH + 4;
        int visible = (h - headerH - footerH) / lineH;

        for (int i = pageScroll; i < lines.length; i++) {
            if (y + lineH > h - footerH) break;
            String line = lines[i];
            if (line.length() == 0) { y += lineH/2; continue; }
            if (line.startsWith("WARNING:")) {
                g.setColor(COLOR_RED); g.setFont(bold());
                g.drawString("!"+line.substring(8), 6, y, Graphics.TOP | Graphics.LEFT);
            } else if (line.startsWith("TIP:")) {
                g.setColor(COLOR_YELLOW); g.setFont(bold());
                g.drawString(">"+line.substring(4), 6, y, Graphics.TOP | Graphics.LEFT);
            } else if (line.endsWith(":") || line.endsWith("only.")) {
                g.setColor(COLOR_HDR_TEXT); g.setFont(bold());
                g.drawString(line, 6, y, Graphics.TOP | Graphics.LEFT);
            } else {
                g.setColor(COLOR_BODY); g.setFont(plain());
                g.drawString(line, 6, y, Graphics.TOP | Graphics.LEFT);
            }
            y += lineH;
        }

        if (pageScroll > 0) {
            g.setColor(COLOR_HDR_TEXT); g.setFont(bold());
            g.drawString("^", w-10, headerH+2, Graphics.TOP | Graphics.LEFT);
        }
        if (pageScroll + visible < lines.length) {
            g.setColor(COLOR_HDR_TEXT); g.setFont(bold());
            g.drawString("v", w-10, h-footerH-14, Graphics.TOP | Graphics.LEFT);
        }
        drawFooter(g, w, h, "UP/DN=SCROLL", "0=BACK");
    }

    public void run() {
        try {
            while (timerRunning && timerSeconds < timerMax) {
                Thread.sleep(1000);
                timerSeconds++;
                repaint();
            }
            timerRunning = false;
            if (timerSeconds >= timerMax) {
                timerDone = true;
                try {
                    javax.microedition.lcdui.AlertType.ALARM.playSound(display);
                } catch (Exception e) {}
            }
            repaint();
        } catch (InterruptedException e) {}
    }

    private void startTimer() {
        timerSeconds = 0;
        timerDone    = false;
        timerRunning = true;
        timerThread  = new Thread(this);
        timerThread.start();
    }

    private void stopTimer() {
        timerRunning = false;
        timerDone    = false;
        timerSeconds = 0;
    }

    protected void keyPressed(int keyCode) {
        int action = getGameAction(keyCode);

        if (screen == 0) {
            if (action == UP && selectedItem > 0) {
                selectedItem--;
                if (selectedItem < menuScroll) menuScroll--;
            } else if (action == DOWN && selectedItem < MAIN_MENU.length-1) {
                selectedItem++;
                int visible = (getHeight() - 50) / 18;
                if (selectedItem >= menuScroll + visible) menuScroll++;
            } else if (action == FIRE) {
                step = 0; pageScroll = 0;
                if      (selectedItem == 0) screen = 1;
                else if (selectedItem == 1) screen = 2;
                else if (selectedItem == 2) screen = 3;
                else if (selectedItem == 3) screen = 4;
                else if (selectedItem == 4) screen = 5;
            } else if (keyCode == KEY_NUM0) {
                midlet.notifyDestroyed();
            }

        } else if (screen == 1) {
            if (action == FIRE) {
                if (step == 0) step = 1;
                else if (step == 1) { step = 2; stopTimer(); }
                else if (step == 2) {
                    if (!timerRunning && !timerDone) startTimer();
                    else if (timerRunning) stopTimer();
                    else if (timerDone) step = 3;
                }
                else if (step == 3) { step = 0; stopTimer(); }
            } else if (keyCode == KEY_NUM0) {
                stopTimer(); screen = 0; step = 0;
            }

        } else if (screen == 2) {
            if (action == UP) {
                if (editingHour) inputHour = (inputHour + 1) % 24;
                else inputMinute = (inputMinute + 5) % 60;
            } else if (action == DOWN) {
                if (editingHour) inputHour = (inputHour + 23) % 24;
                else inputMinute = (inputMinute + 55) % 60;
            } else if (action == FIRE) {
                editingHour = !editingHour;
            } else if (keyCode == KEY_NUM0) {
                screen = 0;
            }

        } else if (screen == 3) {
            int lineH   = 16;
            int visible = (getHeight() - 48) / lineH;
            int maxScroll = 48 - visible;
            if (maxScroll < 0) maxScroll = 0;
            if (action == UP && pageScroll > 0)
                pageScroll--;
            else if (action == DOWN && pageScroll < maxScroll)
                pageScroll++;
            else if (keyCode == KEY_NUM0) {
                screen = 0; pageScroll = 0;
            }
        } else if (screen == 4 || screen == 5) {
            String[] lines = screen == 4 ? NO_COMPASS : ABOUT_LINES;
            int lineH   = 16;
            int visible = (getHeight() - 48) / lineH;
            int maxScroll = lines.length - visible;
            if (maxScroll < 0) maxScroll = 0;
            if (action == UP && pageScroll > 0)
                pageScroll--;
            else if (action == DOWN && pageScroll < maxScroll)
                pageScroll++;
            else if (keyCode == KEY_NUM0) {
                screen = 0; pageScroll = 0;
            }
        }

        repaint();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == exitCommand) midlet.notifyDestroyed();
    }
}