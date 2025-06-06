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

export default function Footer() {
  return (
    <footer className="flex flex-row border-b-stone-200 border-b-2 w-full">
      <div className="flex flex-row mt-9 ml-5 whitespace-nowrap">&copy;Florae, 2025</div>
      <div className="flex flex-row items-center justify-around w-full mt-9 mr-30 ml-10 mb-25">
        {footer.map(({ name, subpoints }) => (
          <div key={name}>
            <b className="mb-5 block">{name}</b>
            {subpoints.map((subpoint) => (
              <p className="mb-2" key={subpoint}>
                {subpoint}
              </p>
            ))}
          </div>
        ))}
      </div>
    </footer>
  );
}
