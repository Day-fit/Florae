import rainyNature from '../assets/rainyNature.mp4';

export default function MainSection(){
    return(
        <div>
          <video
            src={rainyNature}
            className="w-full max-h-[calc(100vh-4rem)] object-cover block"
            autoPlay
            loop
            muted
            playsInline
            aria-hidden
          />
        </div>
    );
}