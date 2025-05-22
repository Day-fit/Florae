export default function Button({ buttonText, icon, ...props }) {
    return (
        <button {...props} className={`flex items-center ${props.className || ""}`}>
            {icon && <span className="mr-2">{icon}</span>}
            {buttonText}
        </button>
    );
}