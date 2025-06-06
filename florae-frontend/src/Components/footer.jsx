/**
 * Footer is a layout component for displaying application footer content,
 * such as links or branding.
 *
 * Usage:
 * ```
 * <Footer />
 * ```
 */


import { footer } from '../util/footer-data.js';

export default function Footer(){
    return(
        <footer className="flex flex-col md:flex-row border-b-stone-200 border-b-2 w-full">
            <div className="flex flex-row mt-9 ml-10 md:ml-5 whitespace-nowrap">
                &copy;Florae, 2025
            </div>
            <div className="flex flex-col md:flex-row items-left md:items-center md:justify-around w-full mt-9 md:mr-30 pl-10 md:mb-25">
            {footer.map(({name, subpoints}) => (
                <div className="mb-6 md:mb-0" key={name}>
                    <b className="mb-5 block">{name}</b>
                    {subpoints.map(subpoint => (
                        <p className="mb-2" key={subpoint}>{subpoint}</p>
                    ))}
            </div>
        ))}
      </div>
    </footer>
  );
}
