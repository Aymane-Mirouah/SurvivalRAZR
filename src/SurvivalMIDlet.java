import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

public class SurvivalMIDlet extends MIDlet {
    private Display display;
    private SurvivalCanvas canvas;

    public void startApp() {
        display = Display.getDisplay(this);
        canvas = new SurvivalCanvas(display, this);
        display.setCurrent(canvas);
    }
    public void pauseApp() {}
    public void destroyApp(boolean unconditional) {}
}

class SurvivalCanvas extends Canvas implements CommandListener {
    private Display display;
    private MIDlet midlet;

    private static final int COLOR_BG       = 0x1a3010;
    private static final int COLOR_HDR_BG   = 0x0d1f08;
    private static final int COLOR_HDR_TEXT = 0x7ecf3a;
    private static final int COLOR_BODY     = 0xc8c8c8;
    private static final int COLOR_YELLOW   = 0xffcc00;
    private static final int COLOR_RED      = 0xff4444;
    private static final int COLOR_SELECTED = 0x3a6025;
    private static final int COLOR_BORDER   = 0x4a7a20;

    private static final int KEY_SOFT_LEFT  = -6;
    private static final int KEY_SOFT_RIGHT = -7;

    private int screen        = 0;
    private int selectedItem  = 0;
    private int subSelected   = 0;
    private int contentScroll = 0;

    private Command exitCommand;

    private static final String[] MAIN_MENU = {
        "1. Fire", "2. Water", "3. Food",
        "4. Shelter", "5. Navigation",
        "6. First Aid", "7. About"
    };

    private static final String[][] SUB_MENUS = {
        { "Basic Principles", "Site Selection", "Fire Materials", "How to Build", "How to Light" },
        { "Water Sources", "Still Construction", "Purification", "Filtration" },
        { "Animals for Food", "Traps & Snares", "Fishing", "Plants", "Cooking" },
        { "Site Selection", "Types of Shelter", "Desert Shelter", "Cold Shelter" },
        { "Using the Sun", "Using Stars", "Shadow Method", "Map & Compass" },
        { "Lifesaving Steps", "Bone & Joint", "Bites & Stings", "Wounds", "Heat & Cold" }
    };

    private static final String[][][] ALL_CONTENT = {
        // FIRE
        {
            { "BASIC PRINCIPLES",
              "Fire can mean life or death.",
              "Fire can:",
              "- Purify water",
              "- Cook food",
              "- Signal rescue",
              "- Provide warmth",
              "- Keep animals away",
              "THE FIRE TRIANGLE:",
              "Air + Heat + Fuel",
              "Remove one = fire dies.",
              "WARNING:Fire reveals",
              "your position via smoke",
              "and light at night.",
              "TIP:Always weigh need",
              "for fire vs. security." },
            { "SITE SELECTION",
              "Before building consider:",
              "- Terrain and climate",
              "- Materials available",
              "- Time you have",
              "- Security concerns",
              "Good spot:",
              "- Protected from wind",
              "- Near your shelter",
              "- Has fuel nearby",
              "Clear 1m circle around",
              "fire to stop spreading.",
              "DAKOTA HOLE:",
              "Dig a hole in ground.",
              "Dig upwind tunnel for air.",
              "Conceals fire well.",
              "WARNING:Never use wet",
              "or porous rocks!",
              "They explode when heated." },
            { "FIRE MATERIALS",
              "TINDER (lights from spark):",
              "- Birch bark",
              "- Dry grass or ferns",
              "- Fine wood shavings",
              "- Dead evergreen needles",
              "- Pocket lint",
              "- Charred cloth",
              "KINDLING (feeds tinder):",
              "- Small dry twigs",
              "- Strips of wood",
              "- Heavy cardboard",
              "FUEL (burns steady):",
              "- Dry standing wood",
              "- Dead branches",
              "TIP:Tinder must be",
              "completely dry." },
            { "HOW TO BUILD",
              "TEPEE METHOD:",
              "Arrange tinder in cone.",
              "Light the center.",
              "Works with wet wood.",
              "LEAN-TO METHOD:",
              "Push stick at 30 degrees.",
              "Tinder under it.",
              "Lean kindling on stick.",
              "CROSS-DITCH METHOD:",
              "Scratch cross in ground.",
              "Tinder in middle.",
              "Build kindling pyramid.",
              "PYRAMID METHOD:",
              "Stack logs in layers.",
              "Light fire on top.",
              "Burns all night.",
              "TIP:Light from upwind." },
            { "HOW TO LIGHT",
              "MODERN METHODS:",
              "- Waterproof matches",
              "- Convex lens",
              "  Focus sun on tinder",
              "- Metal match + knife",
              "  Scrape for sparks",
              "- Battery + wire",
              "  Touch wires to ignite",
              "PRIMITIVE METHODS:",
              "- Flint and steel",
              "  Strike for sparks",
              "- Fire plow",
              "  Plow hardwood in groove",
              "- Bow and drill",
              "  Spin drill with bow",
              "TIP:Practice primitive",
              "methods before you need!" }
        },
        // WATER
        {
            { "WATER SOURCES",
              "You survive 3 days max",
              "without water.",
              "GOOD SOURCES:",
              "- Rainwater (safest)",
              "- Morning dew on leaves",
              "- Running streams",
              "- Springs",
              "- Digging near dry riverbeds",
              "RISKY SOURCES:",
              "- Stagnant ponds",
              "- Slow moving water",
              "AVOID:",
              "- Seawater",
              "- Urine",
              "- Blood",
              "- Alcohol",
              "WARNING:All wild water",
              "should be purified!" },
            { "STILL CONSTRUCTION",
              "SOLAR STILL:",
              "Digs water from ground.",
              "1. Dig hole 90cm wide",
              "   60cm deep",
              "2. Place container",
              "   at bottom",
              "3. Cover with plastic",
              "4. Seal edges with dirt",
              "5. Place rock in center",
              "   to make a cone",
              "Water drips into container.",
              "Produces ~1L per day.",
              "TIP:Add green plants",
              "to hole for more water.",
              "TIP:Build in sunny spot." },
            { "PURIFICATION",
              "BOILING (most reliable):",
              "Boil water for 1 minute.",
              "At high altitude: 3 min.",
              "Let cool before drinking.",
              "PURIFICATION TABLETS:",
              "Follow tablet instructions.",
              "Wait 30 minutes.",
              "Less effective in cold",
              "or cloudy water.",
              "IMPROVISED FILTER:",
              "Layer in a container:",
              "- Grass (top)",
              "- Sand",
              "- Charcoal",
              "- Small rocks (bottom)",
              "WARNING:Filtering alone",
              "does not kill bacteria.",
              "Always boil after!" },
            { "FILTRATION DEVICES",
              "GRASS FILTER:",
              "Stuff grass into a tube.",
              "Pour water through it.",
              "Removes large particles.",
              "SAND FILTER:",
              "Use cloth as base.",
              "Add layers of:",
              "- Coarse sand",
              "- Fine sand",
              "- Charcoal from fire",
              "Improves taste and",
              "removes particles.",
              "CLOTH FILTER:",
              "Pour through several",
              "layers of cloth.",
              "Basic particle removal.",
              "WARNING:Always boil",
              "after any filtration!" }
        },
        // FOOD
        {
            { "ANIMALS FOR FOOD",
              "You survive 3 weeks",
              "without food.",
              "BEST SOURCES:",
              "- Insects (high protein)",
              "  Grubs, grasshoppers",
              "  crickets, ants",
              "- Fish from streams",
              "- Frogs and snakes",
              "- Small mammals",
              "INSECTS TO AVOID:",
              "- Bright colored ones",
              "- Hairy ones",
              "- Bad smelling ones",
              "TIP:Cook all meat.",
              "Never eat raw.",
              "TIP:Insects = more",
              "protein than beef." },
            { "TRAPS & SNARES",
              "SIMPLE SNARE:",
              "Make loop from wire",
              "or strong cord.",
              "Loop size = fist for",
              "rabbits and squirrels.",
              "Place on animal trail.",
              "Height = hand from ground.",
              "DEADFALL TRAP:",
              "Heavy rock on a stick.",
              "Bait under the rock.",
              "Animal hits stick,",
              "rock falls on it.",
              "CHECK TRAPS:",
              "Check every few hours.",
              "Move if no catch in 24hr.",
              "TIP:Use animal scent",
              "to disguise your smell." },
            { "FISHING",
              "IMPROVISED HOOK:",
              "Bend a pin, wire,",
              "or carved bone/wood.",
              "BAIT:",
              "- Worms and grubs",
              "- Insects",
              "- Bits of cloth",
              "- Fish guts",
              "FISHING METHODS:",
              "- Line and hook",
              "- Spear fishing",
              "  (shallow clear water)",
              "- Fish trap with sticks",
              "  in stream",
              "- Tickling trout",
              "  (under rocks in streams)",
              "TIP:Fish are more",
              "active at dawn and dusk." },
            { "PLANTS",
              "UNIVERSAL EDIBILITY TEST:",
              "1. Smell - avoid bad odor",
              "2. Skin test - rub on wrist",
              "   Wait 15 minutes",
              "3. Lip test - touch to lip",
              "   Wait 3 minutes",
              "4. Tongue test - on tongue",
              "   Wait 15 minutes",
              "5. Chew - do not swallow",
              "   Wait 15 minutes",
              "6. Eat small amount",
              "   Wait 8 hours",
              "AVOID plants with:",
              "- Milky or colored sap",
              "- Beans or seeds in pods",
              "- Bitter almond smell",
              "- Spines or fine hairs",
              "WARNING:When in doubt",
              "do not eat the plant!" },
            { "COOKING & STORAGE",
              "COOK ALL MEAT:",
              "Raw meat = parasites",
              "and bacteria.",
              "Boil when possible.",
              "Roast over fire if not.",
              "PRESERVING FOOD:",
              "DRYING (jerky):",
              "Cut meat into thin strips.",
              "Hang near fire or in sun.",
              "Dry until brittle.",
              "SMOKING:",
              "Smoke meat over low fire.",
              "Adds flavor + preserves.",
              "STORAGE TIPS:",
              "- Keep food off ground",
              "- Hang from tree branch",
              "  away from camp",
              "- Keep away from water",
              "TIP:Smell food before",
              "eating. Bad smell = bad food." }
        },
        // SHELTER
        {
            { "SITE SELECTION",
              "Shelter protects from:",
              "- Wind and rain",
              "- Extreme heat or cold",
              "- Insects and animals",
              "GOOD SITE HAS:",
              "- Natural windbreak",
              "- Flat dry ground",
              "- Materials nearby",
              "- Near water but",
              "  above flood level",
              "AVOID:",
              "- Dead trees overhead",
              "- Low ground (flooding)",
              "- Animal trails",
              "- Insect nests",
              "TIP:Build small shelters.",
              "Your body heats them",
              "faster and better." },
            { "TYPES OF SHELTER",
              "LEAN-TO:",
              "Simplest and fastest.",
              "One angled wall.",
              "Good in mild weather.",
              "DEBRIS HUT:",
              "Best insulation.",
              "Pile leaves and branches",
              "over a frame.",
              "A-FRAME:",
              "Ridge pole + two sticks.",
              "Cover with branches.",
              "Good all-round shelter.",
              "TARP SHELTER:",
              "If you have a tarp,",
              "tie between two trees.",
              "Many configurations.",
              "TIP:Insulate the floor",
              "first. Ground steals",
              "heat faster than wind." },
            { "DESERT SHELTER",
              "MAIN THREATS:",
              "- Extreme heat in day",
              "- Cold at night",
              "- Dehydration",
              "- Sandstorms",
              "DESERT RULES:",
              "- Travel at night only",
              "- Rest in shade by day",
              "- Never remove clothing",
              "  (protects from sun)",
              "SHADE SHELTER:",
              "Dig a trench 45-60cm",
              "deep. Cover with tarp",
              "or branches.",
              "Temp inside is much",
              "cooler than outside.",
              "WARNING:Never rest",
              "on hot sand. Use brush",
              "or leaves as insulation." },
            { "COLD SHELTER",
              "SNOW CAVE:",
              "Best in deep snow.",
              "1. Dig into snow slope",
              "2. Tunnel up at angle",
              "3. Make sleeping shelf",
              "   above entrance",
              "4. Poke air holes",
              "5. Block entrance",
              "   with pack or snow",
              "Inside temp stays near 0.",
              "Much warmer than outside.",
              "QUINZHEE:",
              "Pile snow into mound.",
              "Wait 2 hours to harden.",
              "Hollow out inside.",
              "WARNING:Mark your",
              "snow shelter so people",
              "do not step on it.",
              "TIP:Always have",
              "ventilation hole!" }
        },
        // NAVIGATION
        {
            { "USING THE SUN",
              "Sun rises EAST.",
              "Sun sets WEST.",
              "NORTHERN HEMISPHERE:",
              "At noon sun is due SOUTH.",
              "SOUTHERN HEMISPHERE:",
              "At noon sun is due NORTH.",
              "WATCH METHOD:",
              "Point hour hand at sun.",
              "Halfway between hand",
              "and 12 = South (N.Hem)",
              "STICK METHOD:",
              "Plant stick in ground.",
              "Morning shadow = WEST.",
              "Evening shadow = EAST.",
              "TIP:Even on cloudy days",
              "a bright spot in clouds",
              "shows sun position." },
            { "USING STARS",
              "NORTHERN HEMISPHERE:",
              "Find POLARIS (North Star).",
              "It points directly North.",
              "HOW TO FIND POLARIS:",
              "1. Find Big Dipper",
              "2. Draw line through",
              "   the two outer stars",
              "   of the cup",
              "3. Extend line 5x",
              "4. That is Polaris",
              "SOUTHERN HEMISPHERE:",
              "Use the Southern Cross.",
              "Draw line from top",
              "to bottom star x4.5.",
              "That point = South.",
              "TIP:Stars near the",
              "horizon are unreliable.",
              "Use stars overhead." },
            { "SHADOW METHOD",
              "SHADOW TIP METHOD:",
              "Works on sunny days.",
              "1. Push stick in ground",
              "2. Mark shadow tip",
              "   with a stone",
              "3. Wait 15-20 minutes",
              "4. Mark new shadow tip",
              "5. Draw line between",
              "   the two marks",
              "This line runs",
              "WEST to EAST.",
              "Stand with first mark",
              "on your left = facing",
              "NORTH.",
              "TIP:Works anywhere",
              "in the world.",
              "TIP:Longer wait =",
              "more accurate result." },
            { "MAP & COMPASS",
              "READING A MAP:",
              "- Contour lines = hills",
              "- Closer lines = steeper",
              "- Blue = water",
              "- Green = forest",
              "- Scale shows distance",
              "USING A COMPASS:",
              "- Red needle = North",
              "- Rotate housing to align",
              "  needle with N mark",
              "- Travel direction arrow",
              "  shows your bearing",
              "NO COMPASS?",
              "Use watch, stars, or",
              "shadow tip method.",
              "TIP:Always check map",
              "against terrain features",
              "like rivers and hills.",
              "WARNING:Metal objects",
              "affect compass readings." }
        },
        // FIRST AID
        {
            { "LIFESAVING STEPS",
              "CHECK IN ORDER:",
              "1. Is scene safe?",
              "2. Is person breathing?",
              "3. Is there bleeding?",
              "4. Is there shock?",
              "CPR IF NOT BREATHING:",
              "30 chest compressions",
              "2 rescue breaths",
              "Repeat until help arrives",
              "Rate: 100-120/minute",
              "RECOVERY POSITION:",
              "If breathing but",
              "unconscious - roll",
              "onto their side.",
              "WARNING:Do not move",
              "if spine injury suspected.",
              "TIP:Stay calm.",
              "Panic costs lives." },
            { "BONE & JOINT",
              "SIGNS OF FRACTURE:",
              "- Pain and swelling",
              "- Deformity",
              "- Cannot use limb",
              "- Grinding sound",
              "TREATMENT:",
              "1. Do not straighten",
              "2. Immobilize as found",
              "3. Splint above and",
              "   below the break",
              "4. Use sticks and cloth",
              "5. Check circulation",
              "   (feel fingers/toes)",
              "SPRAIN:",
              "Rest, elevate, cool",
              "with wet cloth.",
              "WARNING:Spine injuries",
              "are life threatening.",
              "Do not move person!" },
            { "BITES & STINGS",
              "SNAKE BITE:",
              "1. Keep person still",
              "2. Remove watches/rings",
              "3. Keep bite BELOW heart",
              "4. Get help fast",
              "WARNING:Do NOT:",
              "- Cut and suck",
              "- Apply tourniquet",
              "- Apply ice",
              "- Give alcohol",
              "INSECT STING:",
              "Remove stinger.",
              "Apply cold compress.",
              "Watch for allergic reaction.",
              "SIGNS OF REACTION:",
              "- Swelling face/throat",
              "- Difficulty breathing",
              "- Hives all over body",
              "TIP:Note snake appearance",
              "to describe to doctors." },
            { "WOUNDS",
              "BLEEDING CONTROL:",
              "1. Apply direct pressure",
              "2. Use clean cloth",
              "3. Elevate if possible",
              "4. Do not remove cloth",
              "   Add more on top",
              "5. Maintain pressure",
              "   10-15 minutes",
              "WOUND CLEANING:",
              "1. Wash hands first",
              "2. Rinse with clean water",
              "3. Remove visible dirt",
              "4. Cover with clean cloth",
              "INFECTED WOUND SIGNS:",
              "- Redness spreading",
              "- Warmth and swelling",
              "- Pus discharge",
              "- Red streaks",
              "WARNING:Infection can",
              "be fatal. Seek help!" },
            { "HEAT & COLD",
              "HEAT EXHAUSTION:",
              "Symptoms:",
              "- Heavy sweating",
              "- Cold pale skin",
              "- Weakness, dizziness",
              "Treatment:",
              "- Move to shade",
              "- Loosen clothing",
              "- Apply cool wet cloths",
              "- Drink water slowly",
              "HEAT STROKE (EMERGENCY):",
              "Symptoms:",
              "- Hot DRY skin",
              "- Confusion",
              "- Loss of consciousness",
              "Treatment:",
              "- Cool body immediately",
              "- Wet cloths + fanning",
              "HYPOTHERMIA:",
              "Symptoms:",
              "- Shivering",
              "- Confusion",
              "- Slow pulse",
              "Treatment:",
              "- Get dry and warm",
              "- Warm core first",
              "- Warm fluids if conscious",
              "WARNING:Remove wet",
              "clothing immediately!" }
        }
    };

    private static final String[] ABOUT_CONTENT = {
        "ABOUT",
        "SURVIVAL MANUAL",
        "For Motorola RAZR V3",
        "",
        "VERSION: 1.0",
        "LICENSE: GPLv3",
        "",
        "CONTENT FROM:",
        "US Army Field Manual",
        "FM 3-05.70 (Public Domain)",
        "",
        "ORIGINAL APP:",
        "github.com/ligi/",
        "SurvivalManual",
        "by ligi (GPLv3)",
        "",
        "J2ME PORT BY:",
        "Aymane",
        "Marrakech, Morocco",
        "2026",
        "",
        "CONTROLS:",
        "D-pad = navigate",
        "Center = select",
        "0 = back / exit",
        "",
        "TIP:Stay alive out there! :)"
    };

    public SurvivalCanvas(Display display, MIDlet midlet) {
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
        else if (screen == 1) drawSubMenu(g, w, h);
        else if (screen == 2) drawContent(g, w, h);
        else if (screen == 3) drawAbout(g, w, h);
    }

    private Font bold()  { return Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD,  Font.SIZE_SMALL); }
    private Font plain() { return Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL); }

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
        drawHeader(g, "SURVIVAL MANUAL", w);
        int itemH  = 18;
        int startY = 28;
        for (int i = 0; i < MAIN_MENU.length; i++) {
            int y = startY + (i * itemH);
            if (i == selectedItem) {
                g.setColor(COLOR_SELECTED);
                g.fillRect(4, y, w-8, itemH);
                g.setColor(COLOR_YELLOW);
                g.setFont(bold());
            } else {
                g.setColor(COLOR_BODY);
                g.setFont(plain());
            }
            g.drawString(MAIN_MENU[i], 10, y+2, Graphics.TOP | Graphics.LEFT);
        }
        drawFooter(g, w, h, "CTR=SELECT", "0=EXIT");
    }

    private void drawSubMenu(Graphics g, int w, int h) {
        drawHeader(g, MAIN_MENU[selectedItem].substring(3), w);
        String[] menu = SUB_MENUS[selectedItem];
        int itemH  = 20;
        int startY = 28;
        for (int i = 0; i < menu.length; i++) {
            int y = startY + (i * itemH);
            if (i == subSelected) {
                g.setColor(COLOR_SELECTED);
                g.fillRect(4, y, w-8, itemH);
                g.setColor(COLOR_YELLOW);
                g.setFont(bold());
            } else {
                g.setColor(COLOR_BODY);
                g.setFont(plain());
            }
            g.drawString(menu[i], 10, y+3, Graphics.TOP | Graphics.LEFT);
        }
        drawFooter(g, w, h, "CTR=SELECT", "0=BACK");
    }

    private void drawScrollableLines(Graphics g, String[] lines, int startIdx, int w, int h) {
        int lineH    = 17;
        int headerH  = 26;
        int footerH  = 22;
        int y        = headerH + 4;
        int visLines = (h - headerH - footerH) / lineH;

        for (int i = startIdx + contentScroll; i < lines.length; i++) {
            if (y + lineH > h - footerH) break;
            String line = lines[i];
            if (line.length() == 0) { y += lineH/2; continue; }
            if (line.startsWith("WARNING:")) {
                g.setColor(COLOR_RED);
                g.setFont(bold());
                g.drawString("!"+line.substring(8), 6, y, Graphics.TOP | Graphics.LEFT);
            } else if (line.startsWith("TIP:")) {
                g.setColor(COLOR_YELLOW);
                g.setFont(bold());
                g.drawString(">"+line.substring(4), 6, y, Graphics.TOP | Graphics.LEFT);
            } else if (line.endsWith(":")) {
                g.setColor(COLOR_YELLOW);
                g.setFont(bold());
                g.drawString(line, 6, y, Graphics.TOP | Graphics.LEFT);
            } else {
                g.setColor(COLOR_BODY);
                g.setFont(plain());
                g.drawString(line, 6, y, Graphics.TOP | Graphics.LEFT);
            }
            y += lineH;
        }

        // scroll indicators
        if (contentScroll > 0) {
            g.setColor(COLOR_HDR_TEXT);
            g.setFont(bold());
            g.drawString("^", w-10, headerH+2, Graphics.TOP | Graphics.LEFT);
        }
        if (startIdx + contentScroll + visLines < lines.length) {
            g.setColor(COLOR_HDR_TEXT);
            g.setFont(bold());
            g.drawString("v", w-10, h-footerH-14, Graphics.TOP | Graphics.LEFT);
        }
    }

    private void drawContent(Graphics g, int w, int h) {
        String[] lines = ALL_CONTENT[selectedItem][subSelected];
        drawHeader(g, lines[0], w);
        drawScrollableLines(g, lines, 1, w, h);
        drawFooter(g, w, h, "UP/DN=SCROLL", "0=BACK");
    }

    private void drawAbout(Graphics g, int w, int h) {
        drawHeader(g, ABOUT_CONTENT[0], w);
        drawScrollableLines(g, ABOUT_CONTENT, 1, w, h);
        drawFooter(g, w, h, "UP/DN=SCROLL", "0=BACK");
    }

    protected void keyPressed(int keyCode) {
        int action = getGameAction(keyCode);

        if (screen == 0) {
            if (action == UP && selectedItem > 0)
                selectedItem--;
            else if (action == DOWN && selectedItem < MAIN_MENU.length-1)
                selectedItem++;
            else if (action == FIRE) {
                if (selectedItem == 6) { screen = 3; contentScroll = 0; }
                else { screen = 1; subSelected = 0; }
            }
            else if (keyCode == KEY_NUM0)
                midlet.notifyDestroyed();

        } else if (screen == 1) {
            String[] menu = SUB_MENUS[selectedItem];
            if (action == UP && subSelected > 0)
                subSelected--;
            else if (action == DOWN && subSelected < menu.length-1)
                subSelected++;
            else if (action == FIRE)
                { screen = 2; contentScroll = 0; }
            else if (keyCode == KEY_NUM0)
                screen = 0;

        } else if (screen == 2) {
            String[] lines   = ALL_CONTENT[selectedItem][subSelected];
            int lineH        = 17;
            int visibleLines = (getHeight() - 48) / lineH;
            int maxScroll    = lines.length - 1 - visibleLines;
            if (maxScroll < 0) maxScroll = 0;
            if (action == UP && contentScroll > 0)
                contentScroll--;
            else if (action == DOWN && contentScroll < maxScroll)
                contentScroll++;
            else if (keyCode == KEY_NUM0)
                screen = 1;

        } else if (screen == 3) {
            int lineH        = 17;
            int visibleLines = (getHeight() - 48) / lineH;
            int maxScroll    = ABOUT_CONTENT.length - 1 - visibleLines;
            if (maxScroll < 0) maxScroll = 0;
            if (action == UP && contentScroll > 0)
                contentScroll--;
            else if (action == DOWN && contentScroll < maxScroll)
                contentScroll++;
            else if (keyCode == KEY_NUM0)
                screen = 0;
        }

        repaint();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == exitCommand) midlet.notifyDestroyed();
    }
}