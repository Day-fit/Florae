export default function TextComponent({ icon, title, paragraph }) {
  return (
    <div className="flex flex-row items-start w-full">
      <div className="flex-shrink-0 mr-4 flex items-start pt-1">
        {icon}
      </div>
      <div className="flex flex-col">
        <h1 className="text-2xl font-semibold text-green-800 mb-2">{title}</h1>
        <p className="text-base text-gray-700">{paragraph}</p>
      </div>
    </div>

  );
}
