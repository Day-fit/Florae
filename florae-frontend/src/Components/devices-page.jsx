import { use } from 'react';
import { UserContext } from '../store/user-context.jsx';
import InformationComponent from './information-component.jsx';
import { devicesVisitorContent } from '../util/devices-page-data.js';

export default function DevicesPage({ setModal }) {
  const { isLogged } = use(UserContext);

  return (
    <>
      {isLogged && <div></div>}
      {!isLogged && (
        <div>
          <InformationComponent
            setModal={setModal}
            showFor="not-logged-in"
            visitorContent={devicesVisitorContent}
          />
        </div>
      )}
    </>
  );
}
