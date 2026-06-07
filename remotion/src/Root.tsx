import { Composition } from "remotion";
import { PromoVideo } from "./compositions/PromoVideo";

const FPS = 30;
const TOTAL_SECONDS = 55; // 3 intro + 45 showcase + 7 outro

export function RemotionRoot() {
  return (
    <Composition
      id="CashlyticsPromo"
      component={PromoVideo}
      durationInFrames={TOTAL_SECONDS * FPS}
      fps={FPS}
      width={1080}
      height={1920}
    />
  );
}
