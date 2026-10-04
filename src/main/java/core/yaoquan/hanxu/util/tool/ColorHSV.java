package core.yaoquan.hanxu.util.tool;

import org.jetbrains.annotations.NotNull;

public final class ColorHSV {
    private float h;
    private float s;
    private float v;

    public ColorHSV(float h, float s, float v) {
        this.h = h;
        this.s = s;
        this.v = v;
    }

    public float getH() {
        return h;
    }

    public float getS() {
        return s;
    }

    public float getV() {
        return v;
    }

    private static final float SATURATION_THRESHOLD = 0.1f;

    public static @NotNull ColorHSV fromRGB(int colorRGB) {
        float colorR = ((colorRGB >> 16) & 0xFF) / 255.0f;
        float colorG = ((colorRGB >> 8) & 0xFF) / 255.0f;
        float colorB = (colorRGB & 0xFF) / 255.0f;

        float maximum = Math.max(colorR, Math.max(colorG, colorB));
        float minimum = Math.min(colorR, Math.min(colorG, colorB));
        float delta = maximum - minimum;

        // Calculating: Hue.
        float colorH;
        if (delta < 1e-6f) {
            // Undefined color.
            colorH = 0f;
        }
        else if (maximum == colorR) {
            colorH = 60f * (((colorG - colorB) / delta) % 6f);
        }
        else if (maximum == colorG) {
            colorH = 60f * (((colorB - colorR) / delta) + 2f);
        }
        else {
            colorH = 60f * (((colorR - colorG) / delta) + 4f);
        }

        if (colorH < 0f) {
            colorH += 360f;
        }

        // Calculating: Saturation.
        float colorS = maximum < 1e-6f? 0f : delta / maximum;

        // Where the maximum is the brightness.
        return new ColorHSV(colorH, colorS, maximum);
    }

    public static int toRGB(float colorH, float colorS, float colorV) {
        // Transform.
        float chroma = colorV * colorS;
        float x = chroma * (1f - Math.abs((colorH / 60f) % 2f - 1f));
        float match = colorV - chroma;

        float RelativeR, RelativeG, RelativeB;
        if (colorH < 60f) {
            RelativeR = chroma;
            RelativeG = x;
            RelativeB = 0f;
        }
        else if (colorH < 120f) {
            RelativeR = x;
            RelativeG = chroma;
            RelativeB = 0f;
        }
        else if (colorH < 180f) {
            RelativeR = 0f;
            RelativeG = chroma;
            RelativeB = x;
        }
        else if (colorH < 240f) {
            RelativeR = 0f;
            RelativeG = x;
            RelativeB = chroma;
        }
        else if (colorH < 300f) {
            RelativeR = x;
            RelativeG = 0f;
            RelativeB = chroma;
        }
        else {
            RelativeR = chroma;
            RelativeG = 0f;
            RelativeB = x;
        }

        int colorR = Math.round((RelativeR + match) * 255f);
        int colorG = Math.round((RelativeG + match) * 255f);
        int colorB = Math.round((RelativeB + match) * 255f);

        colorR = Math.clamp(colorR, 0, 255);
        colorG = Math.clamp(colorG, 0, 255);
        colorB = Math.clamp(colorB, 0, 255);

        return (colorR << 16) | (colorG << 8) | colorB;
    }

    public int toRGB() {
        return toRGB(this.h, this.s, this.v);
    }

    public static int interpolate(int fromRGB, int toRGB, float transition) {
        transition = Math.clamp(transition, 0f, 1f);

        if (transition <= 0f) {
            return fromRGB;
        }
        if (transition >= 1f) {
            return toRGB;
        }

        ColorHSV fromHSV = fromRGB(fromRGB);
        ColorHSV toHSV = fromRGB(toRGB);

        if (fromHSV.getS() < SATURATION_THRESHOLD && toHSV.getS() >= SATURATION_THRESHOLD) {
            fromHSV.h = toHSV.h;
        }
        else if (toHSV.getS() < SATURATION_THRESHOLD && fromHSV.getS() >= SATURATION_THRESHOLD) {
            toHSV.h = fromHSV.h;
        }

        float delta = toHSV.getH() - fromHSV.getH();
        if (delta > 180f) {
            delta -= 360f;
        }
        else if (delta < -180f) {
            delta += 360f;
        }

        float newH = fromHSV.getH() + delta * transition;
        if (newH < 0f) {
            newH += 360f;
        }
        else if (newH >= 360f) {
            newH -= 360f;
        }

        float newS = fromHSV.getS() + (toHSV.getS() - fromHSV.getS()) * transition;
        float newV = fromHSV.getV() + (toHSV.getV() - fromHSV.getV()) * transition;

        return new ColorHSV(newH, newS, newV).toRGB();
    }
}
