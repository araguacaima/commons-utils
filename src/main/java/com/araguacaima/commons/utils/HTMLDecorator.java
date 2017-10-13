package com.araguacaima.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
public class HTMLDecorator {
    public static final int ACTUAL_RENDERING_TAG_FONT = 1;
    public static final int ACTUAL_RENDERING_TAG_IMG = 0;
    public static final int ACTUAL_RENDERING_TAG_NONE = -1;
    public static final List<String> FONTCSS_TAG_FIELDS = Arrays.asList("fontCssClass", "fontText");
    public static final List<String> IMAGE_TAG_FIELDS = Arrays.asList("imageSource",
            "imageAltText",
            "imageWidth",
            "imageHeight");
    final static String CSS_CLASS_FIELD = "class";
    final static int DEFAULT_IMAGE_HEIGHT = 19;
    final static int DEFAULT_IMAGE_WIDTH = 25;
    final static String FONT_TAG_NAME = "font";
    final static String IMAGE_TAG_ALT_NAME = "alt";
    final static String IMAGE_TAG_HEIGHT_NAME = "height";
    final static String IMAGE_TAG_NAME = "img";
    final static String IMAGE_TAG_SOURCE_NAME = "src";
    final static String IMAGE_TAG_WIDTH_NAME = "width";
    final static String PIXEL_TAG_NAME = "px";
    /**
     * Tiny inner class for decorating elements that needs to be HTML parsed to GeneralResumeTO
     */
    private final static String className = HTMLDecorator.class.getName();
    private static final Logger log = LoggerFactory.getLogger(HTMLDecorator.class);
    private final ReflectionUtils reflectionUtils;
    public int actualRenderingTag = ACTUAL_RENDERING_TAG_NONE;
    public boolean debug = false;
    public String fontCssClass;
    public String fontText;
    public String imageAltText;
    public int imageHeight = DEFAULT_IMAGE_HEIGHT;
    public String imageSource;
    public int imageWidth = DEFAULT_IMAGE_WIDTH;

    @Autowired
    public HTMLDecorator(ReflectionUtils reflectionUtils) {
        this.reflectionUtils = reflectionUtils;

    }

    /**
     * Decorates the font with the incoming font css tag ensured values
     *
     * @return a complete font tag Whether font css decorator is initialized or null otherwise
     */

    public String decorateFontCssTag() {
        StringBuilder fontCss = new StringBuilder();
        actualRenderingTag = ACTUAL_RENDERING_TAG_FONT;
        if (ensureFontCssInitialized()) {
            fontCss.append(StringUtils.LESS_THAN_SYMBOL).append(FONT_TAG_NAME).append(StringUtils.BLANK_SPACE).append(
                    CSS_CLASS_FIELD).append(StringUtils.EQUAL_SYMBOL).append(StringUtils.DOUBLE_QUOTE).append(
                    fontCssClass).append(StringUtils.DOUBLE_QUOTE).append(StringUtils.GREATER_THAN_SYMBOL);
            fontCss.append(fontText);
            fontCss.append(StringUtils.LESS_THAN_SYMBOL).append(StringUtils.SLASH).append(FONT_TAG_NAME).append(
                    StringUtils.GREATER_THAN_SYMBOL);
        } else {
            return null;
        }
        return printTagRendered(fontCss.toString());
    }

    /**
     * Guarantees that fontCss decorator is initialized by asking for the minimal required source field
     *
     * @return true if source is present, false otherwise
     */

    private boolean ensureFontCssInitialized() {
        return !StringUtils.isBlank(this.fontCssClass);
    }

    private String printTagRendered(String tag) {
        String metodo = className + " - printTagRendered: ";
        if (debug) {
            Collection tal = new ArrayList();
            log.debug(metodo + "Tag values - ");
            switch (actualRenderingTag) {
                case ACTUAL_RENDERING_TAG_IMG:
                    tal = IMAGE_TAG_FIELDS;
                    break;
                case ACTUAL_RENDERING_TAG_FONT:
                    tal = FONTCSS_TAG_FIELDS;
                    break;
                default:
                    break;
            }
            for (Object aTal : tal) {
                String field = (String) aTal;
                log.debug("\t" + field + ": " + reflectionUtils.invokeGetter(this, field));
            }
            log.debug(metodo + "Tag rendered: " + tag);
        }
        return tag;
    }

    /**
     * Decorates the img with the incoming images tag ensured values
     *
     * @return a complete img tag Whether img decorator is initialized or null otherwise
     */

    public String decorateImageTag() {
        StringBuilder imgTag = new StringBuilder();
        actualRenderingTag = ACTUAL_RENDERING_TAG_IMG;
        if (ensureImgIsInitialized()) {
            imgTag.append(StringUtils.LESS_THAN_SYMBOL).append(IMAGE_TAG_NAME).append(StringUtils.BLANK_SPACE).append(
                    IMAGE_TAG_SOURCE_NAME).append(StringUtils.EQUAL_SYMBOL).append(StringUtils.DOUBLE_QUOTE).append(
                    imageSource).append(StringUtils.DOUBLE_QUOTE).append(StringUtils.BLANK_SPACE);
            if (!StringUtils.isBlank(imageAltText)) {
                imgTag.append(IMAGE_TAG_ALT_NAME).append(StringUtils.EQUAL_SYMBOL).append(StringUtils.DOUBLE_QUOTE)
                        .append(
                        imageAltText).append(StringUtils.DOUBLE_QUOTE).append(StringUtils.BLANK_SPACE);
            }
            imgTag.append(IMAGE_TAG_WIDTH_NAME).append(StringUtils.EQUAL_SYMBOL).append(StringUtils.DOUBLE_QUOTE)
                    .append(
                    imageWidth).append(PIXEL_TAG_NAME).append(StringUtils.DOUBLE_QUOTE).append(StringUtils.BLANK_SPACE);
            imgTag.append(IMAGE_TAG_HEIGHT_NAME).append(StringUtils.EQUAL_SYMBOL).append(StringUtils.DOUBLE_QUOTE)
                    .append(
                    imageHeight).append(PIXEL_TAG_NAME).append(StringUtils.DOUBLE_QUOTE).append(StringUtils
                    .BLANK_SPACE);
            imgTag.append(StringUtils.GREATER_THAN_SYMBOL);
        } else {
            return null;
        }
        return printTagRendered(imgTag.toString());
    }

    /**
     * Guarantees that image decorator is initialized by asking for the minimal required source field
     *
     * @return true if source is present, false otherwise
     */

    private boolean ensureImgIsInitialized() {
        return !StringUtils.isBlank(this.imageSource);
    }

    public int getActualRenderingTag() {
        return actualRenderingTag;
    }

    public String getFontCssClass() {
        return fontCssClass;
    }

    public String getFontText() {
        return fontText;
    }

    public String getImageAltText() {
        return imageAltText;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public String getImageSource() {
        return imageSource;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    /**
     * Initialize HTMLDecorator with the minimal information required for build a complete html font with css tag
     *
     * @param cssClassName The CSS class name
     * @param text         The text to be rendered
     */

    public void initializeFontCssTag(String cssClassName, String text) {
        this.fontCssClass = cssClassName;
        this.fontText = text;
    }

    /**
     * Initialize HTMLDecorator with the minimal information required for build a complete html img tag
     *
     * @param imageSource  The relative URL path for the image
     * @param imageAltText The alternate text (non required)
     * @param imageWidth   The width of the image (If ommited 25px will be used)
     * @param imageHeight  The height of the image (If ommited 19px will be used)
     */

    public void initializeImageTag(String imageSource, String imageAltText, int imageWidth, int imageHeight) {
        this.imageSource = imageSource;
        this.imageAltText = imageAltText;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    /**
     * Initialize HTMLDecorator with the minimal information required for build a complete html img tag.
     * Assumes default values for width and height values
     *
     * @param imageSource  The relative URL path for the image
     * @param imageAltText The alternate text (non required)
     */

    public void initializeImageTag(String imageSource, String imageAltText) {
        this.imageSource = imageSource;
        this.imageAltText = imageAltText;
        this.imageWidth = DEFAULT_IMAGE_WIDTH;
        this.imageHeight = DEFAULT_IMAGE_HEIGHT;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
