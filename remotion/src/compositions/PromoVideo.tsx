import {
  AbsoluteFill,
  Audio,
  Easing,
  Img,
  Sequence,
  Video,
  interpolate,
  spring,
  staticFile,
  useCurrentFrame,
  useVideoConfig,
} from "remotion";

const TEAL = "#4DD9AC";
const PURPLE = "#7F77DD";
const BG = "#0C0A1A";
const BG2 = "#14112A";
const TEXT = "#F0EEF8";
const TEXT_MUTED = "rgba(240,238,248,0.55)";

// ── helpers ──────────────────────────────────────────────────────────────────

function useFadeIn(startFrame: number, durationFrames = 20) {
  const frame = useCurrentFrame();
  return interpolate(frame, [startFrame, startFrame + durationFrames], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
}

function useSlideUp(startFrame: number, durationFrames = 25) {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const progress = spring({
    frame: frame - startFrame,
    fps,
    config: { damping: 14, stiffness: 80, mass: 0.9 },
  });
  return interpolate(progress, [0, 1], [40, 0]);
}

// ── Intro (0 – 3 s) ──────────────────────────────────────────────────────────

function IntroScene() {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const logoScale = spring({ frame, fps, config: { damping: 12, stiffness: 100 } });
  const taglineOpacity = useFadeIn(18, 20);
  const taglineY = useSlideUp(18, 25);
  const subOpacity = useFadeIn(30, 20);
  const subY = useSlideUp(30, 25);

  return (
    <AbsoluteFill
      style={{
        background: `radial-gradient(ellipse 80% 60% at 50% 40%, ${PURPLE}22, ${BG} 70%)`,
        alignItems: "center",
        justifyContent: "center",
        flexDirection: "column",
        gap: 24,
      }}
    >
      {/* Logo mark */}
      <div
        style={{
          transform: `scale(${logoScale})`,
          width: 100,
          height: 100,
          background: TEAL,
          borderRadius: 28,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          boxShadow: `0 0 60px ${TEAL}55`,
        }}
      >
        <span
          style={{
            fontFamily: "system-ui, sans-serif",
            fontWeight: 800,
            fontSize: 52,
            color: BG,
            letterSpacing: -2,
          }}
        >
          C
        </span>
      </div>

      {/* App name */}
      <div
        style={{
          opacity: taglineOpacity,
          transform: `translateY(${taglineY}px)`,
          fontFamily: "system-ui, sans-serif",
          fontWeight: 700,
          fontSize: 56,
          color: TEXT,
          letterSpacing: -1,
        }}
      >
        Cash<span style={{ color: TEAL }}>lytics</span>
      </div>

      {/* Tagline */}
      <div
        style={{
          opacity: subOpacity,
          transform: `translateY(${subY}px)`,
          fontFamily: "system-ui, sans-serif",
          fontWeight: 400,
          fontSize: 24,
          color: TEXT_MUTED,
          textAlign: "center",
          maxWidth: 600,
          lineHeight: 1.5,
          paddingHorizontal: 40,
        }}
      >
        Your finances.{" "}
        <span style={{ color: TEAL }}>Auto-tracked.</span>
      </div>
    </AbsoluteFill>
  );
}

// ── Feature Badge ─────────────────────────────────────────────────────────────

function FeatureBadge({
  text,
  icon,
  startFrame,
  side = "right",
}: {
  text: string;
  icon: string;
  startFrame: number;
  side?: "left" | "right";
}) {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const appear = spring({
    frame: frame - startFrame,
    fps,
    config: { damping: 14, stiffness: 120 },
  });
  const opacity = interpolate(appear, [0, 1], [0, 1]);
  const x = interpolate(appear, [0, 1], [side === "right" ? 60 : -60, 0]);

  return (
    <div
      style={{
        display: "flex",
        alignItems: "center",
        gap: 12,
        background: `${BG2}ee`,
        border: `1px solid ${TEAL}44`,
        borderRadius: 50,
        paddingTop: 12,
        paddingBottom: 12,
        paddingLeft: 18,
        paddingRight: 24,
        opacity,
        transform: `translateX(${x}px)`,
        backdropFilter: "blur(8px)",
        boxShadow: `0 4px 24px ${BG}88`,
      }}
    >
      <span style={{ fontSize: 28 }}>{icon}</span>
      <span
        style={{
          fontFamily: "system-ui, sans-serif",
          fontWeight: 600,
          fontSize: 22,
          color: TEXT,
        }}
      >
        {text}
      </span>
    </div>
  );
}

// ── Phone Frame + Recording (3 s – 28 s) ─────────────────────────────────────

function ShowcaseScene() {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const phoneScale = spring({ frame, fps, config: { damping: 14, stiffness: 80 } });
  const phoneOpacity = interpolate(phoneScale, [0, 1], [0, 1]);

  const PHONE_W = 520;
  const PHONE_H = 1040;
  const BORDER_R = 52;

  return (
    <AbsoluteFill
      style={{
        background: `radial-gradient(ellipse 90% 70% at 50% 60%, ${TEAL}0f, ${BG} 65%)`,
        alignItems: "center",
        justifyContent: "center",
      }}
    >
      {/* Left-side badges */}
      <div
        style={{
          position: "absolute",
          left: 40,
          top: "50%",
          transform: "translateY(-50%)",
          display: "flex",
          flexDirection: "column",
          gap: 20,
        }}
      >
        <FeatureBadge text="Auto SMS scan" icon="📱" startFrame={10} side="left" />
        <FeatureBadge text="Offline-first" icon="🔒" startFrame={22} side="left" />
        <FeatureBadge text="Finance ledger" icon="📊" startFrame={34} side="left" />
      </div>

      {/* Phone mockup */}
      <div
        style={{
          width: PHONE_W,
          height: PHONE_H,
          borderRadius: BORDER_R,
          background: "#111",
          border: "3px solid rgba(255,255,255,0.12)",
          overflow: "hidden",
          position: "relative",
          opacity: phoneOpacity,
          transform: `scale(${phoneScale})`,
          boxShadow: `0 40px 120px ${BG}cc, 0 0 0 1px rgba(255,255,255,0.06)`,
        }}
      >
        {/* Status bar */}
        <div
          style={{
            position: "absolute",
            top: 0,
            left: 0,
            right: 0,
            height: 40,
            background: BG,
            zIndex: 10,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
          }}
        >
          <div
            style={{
              width: 120,
              height: 24,
              background: "#000",
              borderRadius: 12,
            }}
          />
        </div>

        {/* Screen recording */}
        <Video
          src={staticFile("cashlytics-recording.mp4")}
          style={{
            width: "100%",
            height: "100%",
            objectFit: "cover",
          }}
          startFrom={0}
        />
      </div>

      {/* Right-side badges */}
      <div
        style={{
          position: "absolute",
          right: 40,
          top: "50%",
          transform: "translateY(-50%)",
          display: "flex",
          flexDirection: "column",
          gap: 20,
        }}
      >
        <FeatureBadge text="Smart notes" icon="📝" startFrame={16} side="right" />
        <FeatureBadge text="Google Drive" icon="☁️" startFrame={28} side="right" />
        <FeatureBadge text="Privacy-first" icon="🛡️" startFrame={40} side="right" />
      </div>
    </AbsoluteFill>
  );
}

// ── Outro CTA (28 s – 32 s) ──────────────────────────────────────────────────

function OutroScene() {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const scale = spring({ frame, fps, config: { damping: 14, stiffness: 100 } });
  const opacity = useFadeIn(0, 15);
  const badgeOpacity = useFadeIn(18, 20);
  const badgeY = useSlideUp(18);

  return (
    <AbsoluteFill
      style={{
        background: `radial-gradient(ellipse 80% 60% at 50% 50%, ${TEAL}1a, ${BG} 65%)`,
        alignItems: "center",
        justifyContent: "center",
        flexDirection: "column",
        gap: 36,
      }}
    >
      <div
        style={{
          opacity,
          transform: `scale(${scale})`,
          fontFamily: "system-ui, sans-serif",
          fontWeight: 800,
          fontSize: 64,
          color: TEXT,
          textAlign: "center",
          letterSpacing: -2,
        }}
      >
        Cash<span style={{ color: TEAL }}>lytics</span>
      </div>

      <div
        style={{
          opacity,
          fontFamily: "system-ui, sans-serif",
          fontSize: 28,
          color: TEXT_MUTED,
          textAlign: "center",
        }}
      >
        Your personal finance & notes app
      </div>

      {/* Google Play badge placeholder */}
      <div
        style={{
          opacity: badgeOpacity,
          transform: `translateY(${badgeY}px)`,
          background: TEXT,
          color: BG,
          fontFamily: "system-ui, sans-serif",
          fontWeight: 700,
          fontSize: 26,
          paddingTop: 18,
          paddingBottom: 18,
          paddingLeft: 40,
          paddingRight: 40,
          borderRadius: 60,
          display: "flex",
          alignItems: "center",
          gap: 12,
        }}
      >
        <span style={{ fontSize: 32 }}>▶</span> Get it on Google Play
      </div>

      {/* Privacy note */}
      <div
        style={{
          opacity: badgeOpacity,
          fontFamily: "system-ui, sans-serif",
          fontSize: 20,
          color: `${TEAL}99`,
          display: "flex",
          gap: 24,
        }}
      >
        <span>🔒 Offline-first</span>
        <span>•</span>
        <span>📵 No data selling</span>
        <span>•</span>
        <span>⭐ Premium available</span>
      </div>
    </AbsoluteFill>
  );
}

// ── Root Composition ──────────────────────────────────────────────────────────

export function PromoVideo() {
  const { fps } = useVideoConfig();

  const INTRO_DURATION = Math.round(3 * fps);     // 3 s
  const SHOWCASE_DURATION = Math.round(25 * fps);  // 25 s
  const OUTRO_DURATION = Math.round(5 * fps);      // 5 s

  return (
    <AbsoluteFill style={{ background: BG }}>
      <Sequence durationInFrames={INTRO_DURATION}>
        <IntroScene />
      </Sequence>

      <Sequence from={INTRO_DURATION} durationInFrames={SHOWCASE_DURATION}>
        <ShowcaseScene />
      </Sequence>

      <Sequence from={INTRO_DURATION + SHOWCASE_DURATION} durationInFrames={OUTRO_DURATION}>
        <OutroScene />
      </Sequence>
    </AbsoluteFill>
  );
}
