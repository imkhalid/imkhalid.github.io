import { Composition } from "remotion";
import { PromoVideo } from "./compositions/PromoVideo";

const FPS = 30;
const TOTAL_SECONDS = 33; // 3 intro + 25 showcase + 5 outro

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
