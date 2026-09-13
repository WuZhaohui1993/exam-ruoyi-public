package com.ruoyi.common.utils.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ruoyi.common.utils.StringUtils;

/**
 * HTML 白名单清洗工具。
 */
public class HtmlSanitizer
{
    private static final HTMLFilter RICH_TEXT_FILTER = new HTMLFilter(buildRichTextConfig());

    private HtmlSanitizer()
    {
    }

    public static String cleanRichText(String content)
    {
        return StringUtils.isEmpty(content) ? content : RICH_TEXT_FILTER.filter(content.trim());
    }

    public static String cleanText(String content)
    {
        return StringUtils.isEmpty(content) ? content : EscapeUtil.clean(content.trim());
    }

    private static Map<String, Object> buildRichTextConfig()
    {
        Map<String, Object> conf = new HashMap<String, Object>();
        Map<String, List<String>> allowed = new HashMap<String, List<String>>();

        List<String> noAttrs = new ArrayList<String>();
        allowed.put("p", noAttrs);
        allowed.put("br", noAttrs);
        allowed.put("b", noAttrs);
        allowed.put("strong", noAttrs);
        allowed.put("i", noAttrs);
        allowed.put("em", noAttrs);
        allowed.put("u", noAttrs);
        allowed.put("ul", noAttrs);
        allowed.put("ol", noAttrs);
        allowed.put("li", noAttrs);
        allowed.put("blockquote", noAttrs);
        allowed.put("code", noAttrs);
        allowed.put("pre", noAttrs);

        List<String> linkAttrs = new ArrayList<String>();
        linkAttrs.add("href");
        linkAttrs.add("target");
        allowed.put("a", linkAttrs);

        List<String> imageAttrs = new ArrayList<String>();
        imageAttrs.add("src");
        imageAttrs.add("width");
        imageAttrs.add("height");
        imageAttrs.add("alt");
        allowed.put("img", imageAttrs);

        conf.put("vAllowed", allowed);
        conf.put("vSelfClosingTags", new String[] { "br", "img" });
        conf.put("vNeedClosingTags", new String[] { "p", "a", "b", "strong", "i", "em", "u", "ul", "ol", "li",
                "blockquote", "code", "pre" });
        conf.put("vDisallowed", new String[] {});
        conf.put("vAllowedProtocols", new String[] { "http", "https", "mailto" });
        conf.put("vProtocolAtts", new String[] { "src", "href" });
        conf.put("vRemoveBlanks", new String[] { "a", "b", "strong", "i", "em", "u", "p", "li", "blockquote",
                "code", "pre" });
        conf.put("vAllowedEntities", new String[] { "amp", "gt", "lt", "quot" });
        conf.put("stripComment", true);
        conf.put("encodeQuotes", true);
        conf.put("alwaysMakeTags", false);
        return conf;
    }
}
