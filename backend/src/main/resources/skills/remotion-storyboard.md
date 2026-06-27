# Remotion Storyboard Skill

Use this style when the user wants a polished idea animation or product-style concept demo.

## Intent
- Convert the thought into 4-7 clear scenes.
- Each scene should have one title, one supporting sentence, and one visible visual role.
- Prefer a cinematic 16:9 composition with a calm left caption area and a right-side spatial map.

## Layout Rules
- Keep the first idea as the anchor.
- Reveal later ideas in a readable path, not a dense cloud.
- Avoid more than 7 visible cards.
- Use short labels; long text belongs in the caption, never inside tiny nodes.
- Maintain strong spacing between cards and edges.

## Motion Rules
- One active scene at a time.
- Use scale, opacity, and line reveal only.
- Avoid rotation-heavy, bouncing, particle, or random motion.
- Every transition should communicate sequence or relationship.

## Remotion Handoff
- Treat the result as a Remotion composition: title, scenes, nodes, edges, active scene, progress bar.
- Target `1920x1080`, 30 fps, 12-24 seconds.
- Keep all layout values deterministic so the same prompt renders consistently.
