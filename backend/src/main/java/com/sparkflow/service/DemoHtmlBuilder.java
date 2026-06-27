package com.sparkflow.service;

import com.sparkflow.dto.MindmapResponse;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DemoHtmlBuilder {

    private record Theme(
            String bg,
            String surface,
            String surfaceSoft,
            String text,
            String muted,
            String line,
            String accent,
            String accent2,
            String glow,
            boolean dark
    ) {}

    private record StoryItem(
            String id,
            String eyebrow,
            String title,
            String body,
            double x,
            double y,
            int index
    ) {}

    public String build(MindmapResponse mindmap, String skillStyle) {
        if (mindmap == null || mindmap.nodes() == null || mindmap.nodes().isEmpty()) {
            return buildEmpty();
        }
        Theme theme = switch (skillStyle != null ? skillStyle : "warm-spark") {
            case "minimal-motion" -> new Theme(
                    "#F7F7F4", "#FFFFFF", "#EFEDE7", "#262626", "#6A6A63",
                    "#D8D4C8", "#5C6F68", "#A47551", "rgba(92,111,104,.22)", false);
            case "tech-blueprint" -> new Theme(
                    "#10131D", "#1A2030", "#242C3C", "#EEF0E8", "#AAB2C0",
                    "#3D5168", "#7BDCB5", "#E4B363", "rgba(123,220,181,.25)", true);
            case "remotion-storyboard" -> new Theme(
                    "#F8F6F0", "#FFFFFF", "#ECE7DC", "#23211D", "#706A5E",
                    "#CFC6B6", "#D0643C", "#4C8077", "rgba(208,100,60,.22)", false);
            default -> new Theme(
                    "#FDFBF7", "#FFFDF9", "#F1E9DC", "#463827", "#8B7355",
                    "#D4C4A8", "#A68B5B", "#5F7F71", "rgba(166,139,91,.22)", false);
        };
        return buildStoryboard(mindmap, theme, "remotion-storyboard".equals(skillStyle));
    }

    private String buildStoryboard(MindmapResponse mindmap, Theme theme, boolean remotionMode) {
        Map<String, MindmapResponse.MindmapNode> nodeMap = new LinkedHashMap<>();
        for (MindmapResponse.MindmapNode n : mindmap.nodes()) {
            if (n.id() != null && !n.id().isBlank()) {
                nodeMap.put(n.id(), n);
            }
        }

        String centerId = findCenterId(mindmap, nodeMap);
        List<String> revealOrder = buildRevealOrder(centerId, mindmap.edges(), nodeMap);
        if (revealOrder.size() > 7) {
            revealOrder = new ArrayList<>(revealOrder.subList(0, 7));
        }

        List<StoryItem> items = new ArrayList<>();
        for (int i = 0; i < revealOrder.size(); i++) {
            MindmapResponse.MindmapNode n = nodeMap.get(revealOrder.get(i));
            if (n == null) continue;
            double[] pos = layoutPos(i, revealOrder.size(), remotionMode);
            items.add(new StoryItem(
                    n.id(),
                    safeText(n.nodeType(), i == 0 ? "core" : "step " + i),
                    shortText(prefer(n.summary(), n.title()), i == 0 ? 18 : 14),
                    shortText(prefer(n.detail(), n.title()), i == 0 ? 92 : 72),
                    pos[0],
                    pos[1],
                    i
            ));
        }

        if (items.isEmpty()) {
            return buildEmpty();
        }

        String cards = buildCards(items, theme);
        String rails = buildRails(items);
        String scenes = buildScenes(items);
        String title = shortText(prefer(mindmap.title(), items.get(0).title()), 28);
        return wrapHtml(title, theme, cards, rails, scenes, items.size(), remotionMode);
    }

    private String buildCards(List<StoryItem> items, Theme theme) {
        StringBuilder html = new StringBuilder();
        for (StoryItem item : items) {
            html.append("""
                    <article class="story-card" data-scene="%d" style="--x:%.3f;--y:%.3f">
                      <div class="node-dot"><span>%02d</span></div>
                      <p>%s</p>
                      <h3>%s</h3>
                    </article>
                    """.formatted(
                    item.index(),
                    item.x(),
                    item.y(),
                    item.index() + 1,
                    escapeHtml(item.eyebrow()),
                    escapeHtml(item.title())
            ));
        }
        return html.toString();
    }

    private String buildRails(List<StoryItem> items) {
        StringBuilder html = new StringBuilder();
        for (int i = 1; i < items.size(); i++) {
            StoryItem prev = items.get(i - 1);
            StoryItem cur = items.get(i);
            double x = Math.min(prev.x(), cur.x());
            double y = Math.min(prev.y(), cur.y());
            double dx = Math.abs(cur.x() - prev.x());
            double dy = Math.abs(cur.y() - prev.y());
            html.append("""
                    <span class="rail" data-scene="%d" style="--x:%.3f;--y:%.3f;--w:%.3f;--h:%.3f"></span>
                    """.formatted(i, x, y, Math.max(dx, .045), Math.max(dy, .045)));
        }
        return html.toString();
    }

    private String buildScenes(List<StoryItem> items) {
        StringBuilder html = new StringBuilder();
        for (StoryItem item : items) {
            html.append("""
                    <section class="caption" data-scene="%d">
                      <p>%s</p>
                      <h2>%s</h2>
                      <div>%s</div>
                    </section>
                    """.formatted(
                    item.index(),
                    escapeHtml(item.eyebrow()),
                    escapeHtml(item.title()),
                    escapeHtml(item.body())
            ));
        }
        return html.toString();
    }

    private String wrapHtml(String title, Theme theme, String cards, String rails, String scenes, int total, boolean remotionMode) {
        String composition = remotionMode ? "Remotion storyboard" : "SparkFlow motion";
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width,initial-scale=1">
                <style>
                *{box-sizing:border-box} html,body{margin:0;width:100%%;height:100%%;overflow:hidden}
                body{font-family:Inter,ui-sans-serif,system-ui,-apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif;background:%s;color:%s}
                .composition{position:relative;width:100vw;height:100vh;min-height:360px;padding:clamp(14px,3vw,34px);display:grid;place-items:center}
                .frame{position:relative;width:min(100%%,1180px);aspect-ratio:16/9;max-height:calc(100vh - 28px);border-radius:18px;overflow:hidden;background:
                  radial-gradient(circle at 18%% 22%%,%s 0 1px,transparent 1px),
                  linear-gradient(135deg,%s 0%%,%s 100%%);background-size:28px 28px,100%% 100%%;
                  box-shadow:0 26px 70px rgba(27,23,18,.18);border:1px solid %s}
                .halo{position:absolute;inset:8%% 7%%;border:1px solid %s;border-radius:24px;opacity:.62}
                .title{position:absolute;left:5.2%%;top:5.4%%;right:5%%;display:flex;align-items:flex-start;justify-content:space-between;gap:16px}
                .title h1{margin:0;max-width:66%%;font-size:clamp(18px,3.1vw,42px);line-height:1.04;font-weight:760;letter-spacing:0;color:%s}
                .title span{font-size:11px;text-transform:uppercase;letter-spacing:.12em;color:%s;white-space:nowrap;padding-top:7px}
                .map{position:absolute;inset:15%% 5%% 12%% 42%%}
                .story-card{position:absolute;left:calc(var(--x)*100%%);top:calc(var(--y)*100%%);width:clamp(112px,15vw,190px);
                  min-height:74px;transform:translate(-50%%,-50%%) scale(.88);opacity:.38;padding:12px 12px 12px 14px;border-radius:14px;
                  background:%s;border:1px solid %s;box-shadow:0 16px 35px rgba(25,22,18,.08);transition:transform .55s cubic-bezier(.2,.9,.2,1),opacity .45s,box-shadow .45s,border-color .45s}
                .story-card.is-active{opacity:1;transform:translate(-50%%,-50%%) scale(1.04);border-color:%s;box-shadow:0 18px 45px %s}
                .story-card.is-past{opacity:.72;transform:translate(-50%%,-50%%) scale(.96)}
                .node-dot{position:absolute;left:-12px;top:-12px;width:34px;height:34px;border-radius:50%%;display:grid;place-items:center;background:%s;color:white;font-size:10px;font-weight:800;border:3px solid %s}
                .story-card p{margin:7px 0 7px;font-size:10px;text-transform:uppercase;letter-spacing:.1em;color:%s;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
                .story-card h3{margin:0;font-size:clamp(13px,1.35vw,17px);line-height:1.22;font-weight:720;color:%s;word-break:break-word}
                .rail{position:absolute;left:calc(var(--x)*100%%);top:calc(var(--y)*100%%);width:calc(var(--w)*100%%);height:calc(var(--h)*100%%);border-top:2px solid %s;border-left:2px solid %s;opacity:.18;transition:opacity .45s}
                .rail.is-active,.rail.is-past{opacity:.75}
                .caption{position:absolute;left:5.2%%;bottom:10%%;width:36%%;min-width:260px;opacity:0;transform:translateY(14px);transition:opacity .45s,transform .45s}
                .caption.is-active{opacity:1;transform:translateY(0)}
                .caption p{margin:0 0 10px;font-size:11px;letter-spacing:.14em;text-transform:uppercase;color:%s}
                .caption h2{margin:0 0 12px;font-size:clamp(21px,3vw,38px);line-height:1.06;letter-spacing:0;color:%s}
                .caption div{font-size:clamp(13px,1.35vw,17px);line-height:1.62;color:%s;display:-webkit-box;-webkit-line-clamp:4;-webkit-box-orient:vertical;overflow:hidden}
                .progress{position:absolute;left:5.2%%;right:5.2%%;bottom:5%%;height:4px;border-radius:999px;background:%s;overflow:hidden}
                .bar{display:block;width:100%%;height:100%%;transform-origin:left;transform:scaleX(0);background:linear-gradient(90deg,%s,%s)}
                .is-playing .bar{animation:bar 2.9s linear infinite}
                @keyframes bar{from{transform:scaleX(0)}to{transform:scaleX(1)}}
                @media(max-width:760px){
                  .composition{padding:10px}.frame{height:100%%;aspect-ratio:auto;border-radius:12px}
                  .title h1{max-width:none;font-size:24px}.title span{display:none}
                  .map{inset:32%% 6%% 16%% 6%%}.caption{left:6%%;right:6%%;bottom:8%%;width:auto;min-width:0}
                  .story-card{width:132px}.caption div{-webkit-line-clamp:3}
                }
                </style>
                </head>
                <body>
                <main class="composition is-playing" data-total="%d">
                  <div class="frame">
                    <div class="halo"></div>
                    <header class="title"><h1>%s</h1><span>%s</span></header>
                    <div class="map">%s%s</div>
                    %s
                    <div class="progress"><span class="bar"></span></div>
                  </div>
                </main>
                <script>
                (function(){
                  var root=document.querySelector('.composition');
                  var total=Number(root.dataset.total)||1;
                  var active=0;
                  var timer=null;
                  function setScene(index){
                    active=(index+total)%%total;
                    document.querySelectorAll('[data-scene]').forEach(function(el){
                      var scene=Number(el.dataset.scene);
                      el.classList.toggle('is-active',scene===active);
                      el.classList.toggle('is-past',scene<active);
                    });
                  }
                  function play(){root.classList.add('is-playing');clearInterval(timer);timer=setInterval(function(){setScene(active+1)},2900)}
                  function pause(){root.classList.remove('is-playing');clearInterval(timer)}
                  window.addEventListener('message',function(event){
                    if(!event.data||event.data.type!=='sparkflow-demo')return;
                    if(event.data.action==='play')play();
                    if(event.data.action==='pause')pause();
                    if(event.data.action==='restart'){setScene(0);play();}
                  });
                  setScene(0);
                  play();
                })();
                </script>
                </body>
                </html>
                """.formatted(
                theme.bg(), theme.text(), theme.line(), theme.bg(), theme.surfaceSoft(), theme.line(),
                theme.line(), theme.text(), theme.muted(), theme.surface(), theme.line(), theme.accent(), theme.glow(),
                theme.accent(), theme.surface(), theme.muted(), theme.text(), theme.line(), theme.line(),
                theme.accent(), theme.text(), theme.muted(), theme.line(), theme.accent(), theme.accent2(),
                total, escapeHtml(title), composition, rails, cards, scenes);
    }

    private String findCenterId(MindmapResponse mindmap, Map<String, MindmapResponse.MindmapNode> nodeMap) {
        for (MindmapResponse.MindmapNode n : mindmap.nodes()) {
            if ("custom_anchor".equals(n.nodeType())) return n.id();
        }
        MindmapResponse.MindmapNode best = mindmap.nodes().get(0);
        double bestDist = Double.MAX_VALUE;
        for (MindmapResponse.MindmapNode n : mindmap.nodes()) {
            double px = n.posX() != null ? n.posX() : 400;
            double py = n.posY() != null ? n.posY() : 300;
            double d = Math.hypot(px - 400, py - 300);
            if (d < bestDist) {
                bestDist = d;
                best = n;
            }
        }
        return best.id();
    }

    private List<String> buildRevealOrder(String centerId, List<MindmapResponse.MindmapEdge> edges,
                                          Map<String, MindmapResponse.MindmapNode> nodeMap) {
        Map<String, List<String>> adj = new HashMap<>();
        if (edges != null) {
            for (MindmapResponse.MindmapEdge e : edges) {
                adj.computeIfAbsent(e.sourceId(), k -> new ArrayList<>()).add(e.targetId());
                adj.computeIfAbsent(e.targetId(), k -> new ArrayList<>()).add(e.sourceId());
            }
        }

        List<String> order = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        if (centerId != null && nodeMap.containsKey(centerId)) {
            queue.add(centerId);
            seen.add(centerId);
        }
        while (!queue.isEmpty()) {
            String cur = queue.poll();
            order.add(cur);
            for (String next : adj.getOrDefault(cur, List.of())) {
                if (!seen.contains(next) && nodeMap.containsKey(next)) {
                    seen.add(next);
                    queue.add(next);
                }
            }
        }
        for (String id : nodeMap.keySet()) {
            if (!seen.contains(id)) order.add(id);
        }
        return order;
    }

    private double[] layoutPos(int index, int total, boolean remotionMode) {
        if (index == 0) {
            return remotionMode ? new double[]{.20, .52} : new double[]{.18, .50};
        }
        double[][] spacious = {
                {.48, .26}, {.72, .30}, {.85, .54}, {.66, .76}, {.42, .72}, {.58, .52}
        };
        double[][] simple = {
                {.44, .32}, {.68, .32}, {.80, .58}, {.56, .74}, {.34, .58}, {.56, .50}
        };
        double[][] points = remotionMode ? spacious : simple;
        return points[(index - 1) % points.length];
    }

    private String buildEmpty() {
        MindmapResponse empty = new MindmapResponse(
                "SparkFlow",
                List.of(new MindmapResponse.MindmapNode("core", "core", "Inspiration", "Start here", "Write an idea to generate a clean motion storyboard.", 400.0, 300.0)),
                List.of()
        );
        return build(empty, "remotion-storyboard");
    }

    private static String prefer(String primary, String fallback) {
        return primary != null && !primary.isBlank() ? primary : fallback;
    }

    private static String safeText(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }

    private static String shortText(String value, int max) {
        String s = safeText(value, "");
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max - 1)) + "...";
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
