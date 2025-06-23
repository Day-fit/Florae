/**
 * Footer is a layout component for displaying application footer content,
 * such as links or branding.
 *
 * Usage:
 * ```
 * <Footer />
 * ```
 */

import { footerData } from '../util/footer-data.js';
import { use } from 'react';
import { UiContext } from '../store/ui-context.jsx';

export default function Footer() {
  const { setView } = use(UiContext);

  const handleNavigation = (subpoint) => {
    switch (subpoint) {
      case 'Home':
        setView('home');
        break;
      case 'My Plants':
        setView('plants');
        break;
      case 'Devices':
        setView('devices');
        break;
      case 'Settings':
        setView('settings');
        break;
      case 'About Florae':
        setView('home'); // Could be a separate about page
        break;
      case 'How it works':
        setView('home'); // Could scroll to how it works section
        break;
      case 'Plant recognition':
        setView('plants'); // Could open create plant modal
        break;
      case 'Smart monitoring':
        setView('devices'); // Could show monitoring features
        break;
      case 'Analytics':
        setView('devices'); // Could show analytics section
        break;
      case 'Plant care tips':
        setView('home'); // Could show tips section
        break;
      case 'GitHub Issues':
        window.open('https://github.com/Day-fit/Florae/issues', '_blank');
        break;
      case 'Documentation':
        window.open('https://github.com/Day-fit/Florae/wiki', '_blank');
        break;
      case 'Support':
        window.open('https://github.com/Day-fit/Florae/issues', '_blank');
        break;
      case 'Feedback':
        window.open('https://github.com/Day-fit/Florae/issues', '_blank');
        break;
      default:
        break;
    }
  };

  return (
    <footer className="flex flex-col md:flex-row border-b-stone-200 border-b-2 w-full">
      <div className="flex flex-row mt-9 ml-10 md:ml-5 whitespace-nowrap">&copy;Florae, 2025</div>
      <div className="flex flex-col md:flex-row items-left md:items-center md:justify-around w-full mt-9 md:mr-30 pl-10 md:mb-25">
        {footerData.map((item, index) => (
          <div className="mb-6 md:mb-0" key={index}>
            <h3 className="font-bold text-green-700 mb-3 text-center md:text-left">
              {item.name}
            </h3>
            {item.subpoints && (
              <div className="flex flex-col space-y-1">
                {item.subpoints.map((subpoint, subIndex) => (
                  <button
                    key={subIndex}
                    onClick={() => handleNavigation(subpoint)}
                    className="text-sm text-gray-600 hover:text-green-700 transition-colors duration-200 text-left cursor-pointer hover:underline"
                  >
                    {subpoint}
                  </button>
                ))}
              </div>
            )}
          </div>
        ))}
      </div>
    </footer>
  );
}
