import Button from './button.jsx';
import { MdLocalFlorist } from 'react-icons/md';
import { use } from 'react';
import { UserContext } from '../store/user-context.jsx';

export default function InformationComponent({
  setModal,
  handleTask,
  guestContent,
  visitorContent,
  withOutButton = false,
  showFor = 'both',
}) {
  const { isLogged } = use(UserContext);

  if (showFor !== 'both') {
    if (showFor === 'logged-in' && !isLogged) return null;
    if (showFor === 'not-logged-in' && isLogged) return null;
  }

  return (
    <div className="flex flex-col items-center md:items-start justify-center p-8 pt-28 pb-28 mb-8 bg-gray-200 bg-opacity-90 rounded-lg shadow-lg text-center md:text-left">
      {/* Top part with title etc. */}
      <h1 className="text-4xl font-bold text-green-900 mb-4 w-full">
        {isLogged ? guestContent.title : visitorContent.title}
      </h1>
      <p className="text-lg text-black mb-6">
        {isLogged ? guestContent.paragraph : visitorContent.paragraph}
      </p>
      {!withOutButton && (
        <Button
          buttonText={isLogged ? guestContent.btnText : visitorContent.btnText}
          icon={isLogged ? <MdLocalFlorist /> : undefined}
          onClick={isLogged ? handleTask : () => setModal('register')}
          className="bg-green-700 text-white px-6 py-3 rounded-full font-semibold shadow hover:bg-green-800 transition"
        />
      )}
    </div>
  );
}
