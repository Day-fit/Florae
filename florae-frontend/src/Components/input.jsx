export default function Input({ label, errorMsg, textArea = false, ...props }) {
  return (
    <div className="w-full flex flex-col">
      <label className="text-left mb-1 font-bold text-">{label}</label>
      {textArea ? <textarea {...props} /> : <input {...props} />}
      {errorMsg && (<p className="text-red-800 text-sm">{errorMsg}</p>)}
    </div>
  );
}
