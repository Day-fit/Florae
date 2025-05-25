export default function Input({ label, textArea = false, ...props }) {
  return (
    <div className="w-full flex flex-col">
      <label className="text-left mb-1 font-bold text-">{label}</label>
      {textArea ? <textarea {...props} /> : <input {...props} />}
    </div>
  );
}
