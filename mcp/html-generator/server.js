/**
 * SparkFlow local HTML generator.
 *
 * Start: node server.js
 * Default port: 3100
 * Frontend MCP URL: http://localhost:3100/generate
 */
const http = require('http');

const PORT = process.env.PORT || 3100;
const OLLAMA_URL = process.env.OLLAMA_URL || 'http://localhost:11434';
const OLLAMA_MODEL = process.env.OLLAMA_MODEL || 'qwen2.5';

const STYLE_PROMPTS = {
  'warm-spark': 'warm, soft, editorial presentation palette',
  'minimal-motion': 'minimal grayscale, fine lines, restrained opacity transitions',
  'tech-blueprint': 'dark blueprint canvas, precise architecture-map feel',
  'remotion-storyboard': 'Remotion-style 16:9 storyboard, 4-7 scenes, left caption, right relationship map',
};

async function generateWithOllama(prompt, style, context) {
  const styleHint = STYLE_PROMPTS[style] || STYLE_PROMPTS['warm-spark'];
  const system = [
    'Generate one self-contained HTML file with inline CSS/JS and no CDN.',
    `Style: ${styleHint}.`,
    'Use a controlled storyboard composition: 16:9 stage, one active scene at a time, left caption, right relationship map.',
    'Use 4-7 cards max, deterministic positions, short card labels, and readable spacing.',
    'Avoid random particles, rotating clutter, dense paragraphs inside cards, and overlapping text.',
    'Only output HTML.',
  ].join(' ');
  const user = `Prompt: ${prompt}\nContext: ${context || ''}`;

  const res = await fetch(`${OLLAMA_URL}/api/chat`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      model: OLLAMA_MODEL,
      messages: [
        { role: 'system', content: system },
        { role: 'user', content: user },
      ],
      stream: false,
    }),
  });

  if (!res.ok) throw new Error(`Ollama error: ${res.status}`);
  const data = await res.json();
  let html = data.message?.content || '';
  if (html.startsWith('```')) {
    const start = html.indexOf('<');
    const end = html.lastIndexOf('>');
    if (start >= 0) html = html.slice(start, end + 1);
  }
  return html;
}

function fallbackHtml(prompt) {
  const clean = sanitize(prompt).trim();
  const title = compact(clean).slice(0, 18) || 'SparkFlow';
  const words = clean.split(/[，。,.；;\s]+/).filter(Boolean).slice(0, 5);
  const scenes = [title, ...words].slice(0, 6);
  const body = clean.slice(0, 96) || 'A clean motion storyboard generated locally.';
  const points = [[20, 52], [48, 26], [72, 30], [84, 54], [64, 76], [42, 72]];

  const cards = scenes.map((scene, index) => {
    const [x, y] = points[index] || points[points.length - 1];
    return `<article class="card" data-scene="${index}" style="left:${x}%;top:${y}%"><b>${String(index + 1).padStart(2, '0')}</b><span>${sanitize(scene).slice(0, 14)}</span></article>`;
  }).join('');

  const captions = scenes.map((scene, index) =>
    `<section class="caption" data-scene="${index}"><p>Scene ${index + 1}</p><h2>${sanitize(scene).slice(0, 18)}</h2><div>${body}</div></section>`
  ).join('');

  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>${title}</title>
<style>*{box-sizing:border-box}html,body{margin:0;width:100%;height:100%;overflow:hidden}body{font-family:Inter,system-ui,sans-serif;background:#F8F6F0;color:#23211D}.stage{position:relative;width:100vw;height:100vh;padding:30px;display:grid;place-items:center}.frame{position:relative;width:min(100%,1180px);aspect-ratio:16/9;max-height:calc(100vh - 60px);overflow:hidden;border-radius:18px;background:linear-gradient(135deg,#fff,#ece7dc);border:1px solid #cfc6b6;box-shadow:0 24px 70px rgba(30,24,18,.16)}h1{position:absolute;left:5%;top:5%;right:5%;margin:0;font-size:clamp(24px,4vw,48px);line-height:1.02}.map{position:absolute;inset:18% 5% 13% 42%}.card{position:absolute;width:156px;min-height:78px;transform:translate(-50%,-50%) scale(.9);opacity:.42;padding:16px;border-radius:14px;background:#fff;border:1px solid #cfc6b6;transition:.45s}.card.is-active{opacity:1;transform:translate(-50%,-50%) scale(1.04);border-color:#D0643C;box-shadow:0 18px 45px rgba(208,100,60,.22)}.card.is-past{opacity:.72}.card b{display:block;color:#D0643C;font-size:11px}.card span{display:block;margin-top:8px;font-weight:750;line-height:1.2}.caption{position:absolute;left:5%;bottom:12%;width:35%;opacity:0;transform:translateY(12px);transition:.45s}.caption.is-active{opacity:1;transform:translateY(0)}.caption p{margin:0 0 10px;color:#D0643C;font-size:11px;text-transform:uppercase;letter-spacing:.12em}.caption h2{margin:0 0 12px;font-size:clamp(24px,3vw,40px);line-height:1.04}.caption div{color:#706A5E;line-height:1.6}.progress{position:absolute;left:5%;right:5%;bottom:5%;height:4px;background:#cfc6b6;border-radius:999px;overflow:hidden}.bar{display:block;height:100%;background:linear-gradient(90deg,#D0643C,#4C8077);animation:bar 2.9s linear infinite}@keyframes bar{from{width:0}to{width:100%}}@media(max-width:760px){.stage{padding:10px}.frame{height:100%;aspect-ratio:auto}.map{inset:32% 6% 17%}.caption{left:6%;right:6%;bottom:8%;width:auto}}</style></head>
<body><main class="stage"><div class="frame"><h1>${title}</h1><div class="map">${cards}</div>${captions}<div class="progress"><span class="bar"></span></div></div></main><script>(function(){var active=0,total=${scenes.length};function setScene(i){active=(i+total)%total;document.querySelectorAll('[data-scene]').forEach(function(el){var s=Number(el.dataset.scene);el.classList.toggle('is-active',s===active);el.classList.toggle('is-past',s<active)})}setScene(0);setInterval(function(){setScene(active+1)},2900)})();</script></body></html>`;
}

function sanitize(value) {
  return String(value || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function compact(value) {
  return String(value || '').replace(/\s+/g, '');
}

const server = http.createServer(async (req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  if (req.method === 'GET' && req.url === '/health') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ status: 'ok', service: 'sparkflow-html-generator' }));
    return;
  }

  if (req.method === 'POST' && req.url === '/generate') {
    let body = '';
    req.on('data', (chunk) => { body += chunk; });
    req.on('end', async () => {
      try {
        const { prompt, style, context } = JSON.parse(body);
        let html;
        try {
          html = await generateWithOllama(prompt, style || 'remotion-storyboard', context);
        } catch {
          html = fallbackHtml(prompt);
        }
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ html }));
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: e.message }));
      }
    });
    return;
  }

  res.writeHead(404);
  res.end('Not Found');
});

server.listen(PORT, () => {
  console.log(`SparkFlow HTML generator: http://localhost:${PORT}`);
  console.log('  POST /generate  generate storyboard HTML');
  console.log('  GET  /health    health check');
});
